import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Random;

public class Raytracer {

    private static final double EPSILON = 0.001;
    private static final double LUFT_BRECHUNGSINDEX = 1.0;

    private static final Random ZUFALL = new Random(594568);

    private static final int SCHATTEN_STRAHLEN = 24;
    private static final double LICHT_RADIUS = 2.0;

    private static final int AO_STRAHLEN = 12;
    private static final double AO_MAX_ABSTAND = 3.0;
    private static final double AO_STAERKE = 0.6;

    private static final int REFLEXIONS_STRAHLEN = 6;

    public static void main(String[] args) throws Exception {
        String szenenDatei = args.length > 0 ? args[0] : "szene.txt";
        Szene szene = SzenenParser.ladeSzene(szenenDatei);

        BufferedImage bild = new BufferedImage(szene.breite, szene.hoehe, BufferedImage.TYPE_INT_RGB);

        System.out.println("Lade Szene aus " + szenenDatei + "...");
        System.out.println("Starte Rendering " + szene.breite + "x" + szene.hoehe + "...");

        for (int y = 0; y < szene.hoehe; y++) {
            for (int x = 0; x < szene.breite; x++) {
                Strahl strahl = szene.kamera.erzeugeStrahl(x, y, szene.breite, szene.hoehe);
                Vektor3 farbe = berechneFarbe(strahl, szene, szene.maxTiefe);
                bild.setRGB(x, y, zuRGB(farbe));
            }
        }

        ImageIO.write(bild, "png", new File(szene.ausgabeDatei));
        System.out.println("Bild wurde als " + szene.ausgabeDatei + " gespeichert.");
    }

    static Vektor3 berechneFarbe(Strahl strahl, List<Objekt> objekte, Licht licht, Vektor3 kameraPosition, int tiefe) {
        Szene szene = new Szene();
        szene.objekte = objekte;
        szene.licht = licht;
        szene.kamera = new Kamera(kameraPosition, strahl.richtung, 60);
        return berechneFarbe(strahl, szene, tiefe);
    }

    static Vektor3 berechneFarbe(Strahl strahl, Szene szene, int tiefe) {
        if (tiefe <= 0) {
            return new Vektor3(0, 0, 0);
        }

        Treffer treffer = findeNaechstenTreffer(strahl, szene.objekte);

        if (treffer == null) {
            return szene.hintergrund;
        }

        Material mat = treffer.material;
        Vektor3 N = treffer.normale.normiere();
        Vektor3 I = strahl.richtung.normiere();

        double schattenFaktor = berechneWeichenSchattenFaktor(treffer, szene.licht, szene.objekte);

        double aoFaktor = berechneAmbientOcclusion(treffer, szene.objekte);

        Vektor3 lokaleFarbe = cookTorrance(treffer, szene.licht, I.mal(-1.0))
                .mal(schattenFaktor)
                .mal(aoFaktor);

        Vektor3 reflexionsFarbe = new Vektor3(0, 0, 0);

        if (mat.reflexionsgrad > 0.0 || mat.transparenz > 0.0) {
            reflexionsFarbe = berechneReflexionsFarbe(treffer, I, N, szene, tiefe);
        }

        Vektor3 refraktionsFarbe = new Vektor3(0, 0, 0);
        double fresnel = 0.0;
        boolean totalreflexion = false;

        if (mat.transparenz > 0.0) {
            double n1 = treffer.eintritt ? LUFT_BRECHUNGSINDEX : mat.brechungsindex;
            double n2 = treffer.eintritt ? mat.brechungsindex : LUFT_BRECHUNGSINDEX;
            double eta = n1 / n2;

            double cosThetaI = Math.max(0.0, Math.min(1.0, -N.skalarprodukt(I)));

            Vektor3 refraktionsRichtung = refraktiere(I, N, eta);

            if (refraktionsRichtung == null) {
                totalreflexion = true;
                fresnel = 1.0;
            } else {
                double sin2ThetaT = eta * eta * (1.0 - cosThetaI * cosThetaI);
                double cosThetaT = Math.sqrt(Math.max(0.0, 1.0 - sin2ThetaT));

                fresnel = fresnelBruch(cosThetaI, cosThetaT, n1, n2);

                Strahl refraktionsStrahl = new Strahl(
                        treffer.position.plus(refraktionsRichtung.mal(EPSILON)),
                        refraktionsRichtung
                );

                refraktionsFarbe = berechneFarbe(refraktionsStrahl, szene, tiefe - 1);
            }
        }

        double kTransparenz = mat.transparenz * (1.0 - fresnel);
        double kReflexion = mat.reflexionsgrad + mat.transparenz * fresnel;

        if (totalreflexion) {
            kReflexion = Math.max(kReflexion, mat.transparenz);
            kTransparenz = 0.0;
        }

        double kLokal = Math.max(0.0, 1.0 - mat.reflexionsgrad - mat.transparenz);

        return lokaleFarbe.mal(kLokal)
                .plus(reflexionsFarbe.mal(kReflexion))
                .plus(refraktionsFarbe.mal(kTransparenz));
    }

    private static Treffer findeNaechstenTreffer(Strahl strahl, List<Objekt> objekte) {
        Treffer naechsterTreffer = null;

        for (Objekt objekt : objekte) {
            Treffer treffer = objekt.schneide(strahl);

            if (treffer != null && treffer.entfernung > EPSILON) {
                if (naechsterTreffer == null || treffer.entfernung < naechsterTreffer.entfernung) {
                    naechsterTreffer = treffer;
                }
            }
        }

        return naechsterTreffer;
    }

    private static double berechneWeichenSchattenFaktor(Treffer oberflaeche, Licht licht, List<Objekt> objekte) {
        double summe = 0.0;

        for (int i = 0; i < SCHATTEN_STRAHLEN; i++) {
            Vektor3 zufallsPunktAufLicht = zufallsPunktAufLichtkugel(licht.position, LICHT_RADIUS);
            summe += berechneLichtDurchlass(oberflaeche.position, zufallsPunktAufLicht, objekte);
        }

        return summe / SCHATTEN_STRAHLEN;
    }

    private static double berechneLichtDurchlass(Vektor3 startPunkt, Vektor3 lichtPunkt, List<Objekt> objekte) {
        Vektor3 zumLicht = lichtPunkt.minus(startPunkt);
        double lichtEntfernung = zumLicht.laenge();
        Vektor3 L = zumLicht.normiere();

        Strahl schattenStrahl = new Strahl(startPunkt.plus(L.mal(EPSILON)), L);

        double bisherigeDistanz = EPSILON;
        double durchlass = 1.0;

        while (bisherigeDistanz < lichtEntfernung) {
            Treffer blockierenderTreffer = findeNaechstenTreffer(schattenStrahl, objekte);

            if (blockierenderTreffer == null ||
                    bisherigeDistanz + blockierenderTreffer.entfernung >= lichtEntfernung) {
                return durchlass;
            }

            double transparenz = blockierenderTreffer.material.transparenz;

            if (transparenz <= 0.0) {
                return 0.0;
            }

            durchlass *= transparenz;

            if (durchlass < 0.01) {
                return 0.0;
            }

            bisherigeDistanz += blockierenderTreffer.entfernung + EPSILON;

            Vektor3 neuerUrsprung = blockierenderTreffer.position.plus(L.mal(EPSILON));
            schattenStrahl = new Strahl(neuerUrsprung, L);
        }

        return durchlass;
    }

    private static double berechneAmbientOcclusion(Treffer treffer, List<Objekt> objekte) {
        Vektor3 N = treffer.normale.normiere();
        double abstandsSumme = 0.0;

        for (int i = 0; i < AO_STRAHLEN; i++) {
            Vektor3 richtung = zufallsRichtungHalbkugel(N);
            Strahl aoStrahl = new Strahl(treffer.position.plus(N.mal(EPSILON)), richtung);

            Treffer aoTreffer = findeNaechstenTreffer(aoStrahl, objekte);

            double abstand = AO_MAX_ABSTAND;

            if (aoTreffer != null) {
                abstand = Math.min(AO_MAX_ABSTAND, aoTreffer.entfernung);
            }

            abstandsSumme += abstand;
        }

        double durchschnitt = abstandsSumme / AO_STRAHLEN;
        double faktor = durchschnitt / AO_MAX_ABSTAND;

        return (1.0 - AO_STAERKE) + AO_STAERKE * faktor;
    }

    private static Vektor3 berechneReflexionsFarbe(Treffer treffer, Vektor3 I, Vektor3 N, Szene szene, int tiefe) {
        Vektor3 perfekteRichtung = reflektiere(I, N).normiere();

        int anzahlStrahlen = treffer.material.rauheit > 0.05 ? REFLEXIONS_STRAHLEN : 1;
        double streuung = Math.min(0.75, treffer.material.rauheit);

        Vektor3 summe = new Vektor3(0, 0, 0);

        for (int i = 0; i < anzahlStrahlen; i++) {
            Vektor3 richtung;

            if (anzahlStrahlen == 1) {
                richtung = perfekteRichtung;
            } else {
                richtung = zufallsRichtungUm(perfekteRichtung, streuung);
            }

            Strahl reflexionsStrahl = new Strahl(
                    treffer.position.plus(N.mal(EPSILON)),
                    richtung
            );

            summe = summe.plus(berechneFarbe(reflexionsStrahl, szene, tiefe - 1));
        }

        return summe.mal(1.0 / anzahlStrahlen);
    }

    private static Vektor3 reflektiere(Vektor3 richtung, Vektor3 normale) {
        return richtung.minus(normale.mal(2.0 * richtung.skalarprodukt(normale)));
    }

    private static Vektor3 refraktiere(Vektor3 I, Vektor3 N, double eta) {
        double cosThetaI = Math.max(0.0, Math.min(1.0, -N.skalarprodukt(I)));
        double sin2ThetaT = eta * eta * (1.0 - cosThetaI * cosThetaI);

        if (sin2ThetaT > 1.0) {
            return null;
        }

        double cosThetaT = Math.sqrt(Math.max(0.0, 1.0 - sin2ThetaT));

        return I.mal(eta)
                .plus(N.mal(eta * cosThetaI - cosThetaT))
                .normiere();
    }

    private static double fresnelBruch(double cosThetaI, double cosThetaT, double n1, double n2) {
        double zaehlerS = n1 * cosThetaI - n2 * cosThetaT;
        double nennerS = n1 * cosThetaI + n2 * cosThetaT;

        double zaehlerP = n2 * cosThetaI - n1 * cosThetaT;
        double nennerP = n2 * cosThetaI + n1 * cosThetaT;

        if (Math.abs(nennerS) < EPSILON || Math.abs(nennerP) < EPSILON) {
            return 1.0;
        }

        double Fs = (zaehlerS / nennerS) * (zaehlerS / nennerS);
        double Fp = (zaehlerP / nennerP) * (zaehlerP / nennerP);

        return begrenzen((Fs + Fp) * 0.5);
    }

    // Zufälliger Punkt auf der Lichtkugel
    private static Vektor3 zufallsPunktAufLichtkugel(Vektor3 mittelpunkt, double radius) {
        return mittelpunkt.plus(zufallsEinheitsVektor().mal(radius));
    }

    //Erzeugt eine zufällige Richtung.
    private static Vektor3 zufallsEinheitsVektor() {
        double z = 2.0 * ZUFALL.nextDouble() - 1.0;
        double winkel = 2.0 * Math.PI * ZUFALL.nextDouble();
        double radius = Math.sqrt(Math.max(0.0, 1.0 - z * z));

        double x = radius * Math.cos(winkel);
        double y = radius * Math.sin(winkel);

        return new Vektor3(x, y, z).normiere();
    }

    // Zufällige Richtung über der Oberfläche
    private static Vektor3 zufallsRichtungHalbkugel(Vektor3 normale) {
        Vektor3 richtung = zufallsEinheitsVektor();

        if (richtung.skalarprodukt(normale) < 0.0) {
            richtung = richtung.mal(-1.0);
        }

        return richtung.normiere();
    }

    // Zufällige Richtung nahe an einer perfekten Richtung
    private static Vektor3 zufallsRichtungUm(Vektor3 basisRichtung, double streuung) {
        Vektor3 zufall = zufallsRichtungHalbkugel(basisRichtung);

        return basisRichtung.mal(1.0 - streuung)
                .plus(zufall.mal(streuung))
                .normiere();
    }

    static Vektor3 cookTorrance(Treffer treffer, Licht licht, Vektor3 blickRichtung) {
        Vektor3 N = treffer.normale.normiere();
        Vektor3 L = licht.position.minus(treffer.position).normiere();
        Vektor3 V = blickRichtung.normiere();
        Vektor3 H = L.plus(V).normiere();

        double NdotL = Math.max(0, N.skalarprodukt(L));
        double NdotV = Math.max(0, N.skalarprodukt(V));
        double NdotH = Math.max(0, N.skalarprodukt(H));
        double VdotH = Math.max(0, V.skalarprodukt(H));

        if (NdotL <= 0.0 || NdotV <= 0.0) {
            return new Vektor3(0, 0, 0);
        }

        double rauheit = treffer.material.rauheit;
        double metallisch = treffer.material.metallisch;

        double alpha = rauheit * rauheit;
        double alpha2 = alpha * alpha;

        double nennerD = (NdotH * NdotH) * (alpha2 - 1.0) + 1.0;
        double D = alpha2 / (Math.PI * nennerD * nennerD);

        double k = (rauheit + 1.0) * (rauheit + 1.0) / 8.0;
        double G1L = NdotL / (NdotL * (1.0 - k) + k);
        double G1V = NdotV / (NdotV * (1.0 - k) + k);
        double G = G1L * G1V;

        Vektor3 F0 = new Vektor3(0.04, 0.04, 0.04)
                .mal(1.0 - metallisch)
                .plus(treffer.material.farbe.mal(metallisch));

        Vektor3 F = F0.plus(
                new Vektor3(1, 1, 1)
                        .minus(F0)
                        .mal(Math.pow(1.0 - VdotH, 5.0))
        );

        Vektor3 zaehler = F.mal(D * G);
        double nenner = Math.max(0.001, 4.0 * NdotV * NdotL);
        Vektor3 spiegelung = zaehler.mal(1.0 / nenner);

        Vektor3 diffus = treffer.material.farbe
                .mal(1.0 - metallisch)
                .mal(1.0 / Math.PI);

        return diffus.plus(spiegelung).mal(NdotL * licht.intensitaet);
    }

    static int zuRGB(Vektor3 farbe) {
        int r = (int)(gamma(farbe.x) * 255);
        int g = (int)(gamma(farbe.y) * 255);
        int b = (int)(gamma(farbe.z) * 255);

        return new Color(r, g, b).getRGB();
    }

    static double gamma(double wert) {
        return Math.pow(begrenzen(wert), 1.0 / 2.2);
    }

    static double begrenzen(double wert) {
        return Math.max(0, Math.min(1, wert));
    }
}
