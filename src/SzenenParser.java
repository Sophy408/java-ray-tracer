import java.io.File;
import java.util.List;
import java.util.Scanner;

public class SzenenParser {

    public static Szene ladeSzene(String dateipfad) throws Exception {
        Szene szene = new Szene();
        Scanner scanner = new Scanner(new File(dateipfad));
        int zeilennummer = 0;

        while (scanner.hasNextLine()) {
            zeilennummer++;
            String zeile = scanner.nextLine().trim();

            if (zeile.isEmpty() || zeile.startsWith("#")) {
                continue;
            }

            String[] teile = zeile.split("\\s+");
            String typ = teile[0].toUpperCase();

            try {
                switch (typ) {
                    case "AUFLOESUNG":
                    case "AUFLÖSUNG":
                        pruefeAnzahl(teile, 3, zeilennummer);
                        szene.breite = Integer.parseInt(teile[1]);
                        szene.hoehe = Integer.parseInt(teile[2]);
                        break;

                    case "AUSGABE":
                        pruefeAnzahl(teile, 2, zeilennummer);
                        szene.ausgabeDatei = teile[1];
                        break;

                    case "MAX_TIEFE":
                        pruefeAnzahl(teile, 2, zeilennummer);
                        szene.maxTiefe = Integer.parseInt(teile[1]);
                        break;

                    case "HINTERGRUND":
                        pruefeAnzahl(teile, 4, zeilennummer);
                        szene.hintergrund = new Vektor3(
                                Double.parseDouble(teile[1]),
                                Double.parseDouble(teile[2]),
                                Double.parseDouble(teile[3])
                        );
                        break;

                    case "KAMERA":
                        // px py pz  bx by bz  sichtfeld
                        pruefeAnzahl(teile, 8, zeilennummer);
                        szene.kamera = new Kamera(
                                new Vektor3(
                                        Double.parseDouble(teile[1]),
                                        Double.parseDouble(teile[2]),
                                        Double.parseDouble(teile[3])
                                ),
                                new Vektor3(
                                        Double.parseDouble(teile[4]),
                                        Double.parseDouble(teile[5]),
                                        Double.parseDouble(teile[6])
                                ),
                                Double.parseDouble(teile[7])
                        );
                        break;

                    case "LICHT":
                        // x y z intensität
                        pruefeAnzahl(teile, 5, zeilennummer);
                        szene.licht = new Licht(
                                new Vektor3(
                                        Double.parseDouble(teile[1]),
                                        Double.parseDouble(teile[2]),
                                        Double.parseDouble(teile[3])
                                ),
                                Double.parseDouble(teile[4])
                        );
                        break;

                    case "KUGEL":
                        //x y z radius  r g b  rauheit metallisch reflexion transparenz brechungsindex
                        pruefeAnzahl(teile, 13, zeilennummer);
                        szene.objekte.add(new Kugel(
                                new Vektor3(
                                        Double.parseDouble(teile[1]),
                                        Double.parseDouble(teile[2]),
                                        Double.parseDouble(teile[3])
                                ),
                                Double.parseDouble(teile[4]),
                                liesMaterial(teile, 5)
                        ));
                        break;

                    case "QUADRIK":
                        // A B C D E F G H I J  r g b  rauheit metallisch reflexion transparenz brechungsindex
                        pruefeAnzahl(teile, 19, zeilennummer);
                        Material material = liesMaterial(teile, 11);
                        szene.objekte.add(new Quadrik(
                                Double.parseDouble(teile[1]),
                                Double.parseDouble(teile[2]),
                                Double.parseDouble(teile[3]),
                                Double.parseDouble(teile[4]),
                                Double.parseDouble(teile[5]),
                                Double.parseDouble(teile[6]),
                                Double.parseDouble(teile[7]),
                                Double.parseDouble(teile[8]),
                                Double.parseDouble(teile[9]),
                                Double.parseDouble(teile[10]),
                                new Matrix4(),
                                new Matrix4(),
                                material
                        ));
                        break;

                    default:
                        throw new IllegalArgumentException("Unbekannter Szenenbefehl '" + teile[0] + "'");
                }
            } catch (RuntimeException ex) {
                scanner.close();
                throw new IllegalArgumentException(
                        "Fehler in " + dateipfad + " Zeile " + zeilennummer + ": " + zeile + "\n" + ex.getMessage(),
                        ex
                );
            }
        }

        scanner.close();
        return szene;
    }

    public static List<Objekt> ladeObjekte(String dateipfad) throws Exception {
        return ladeSzene(dateipfad).objekte;
    }

    public static Licht ladeLicht(String dateipfad) throws Exception {
        return ladeSzene(dateipfad).licht;
    }

    private static Material liesMaterial(String[] teile, int start) {
        Vektor3 farbe = new Vektor3(
                Double.parseDouble(teile[start]),
                Double.parseDouble(teile[start + 1]),
                Double.parseDouble(teile[start + 2])
        );

        return new Material(
                farbe,
                Double.parseDouble(teile[start + 3]), // rauheit
                Double.parseDouble(teile[start + 4]), // metallisch
                Double.parseDouble(teile[start + 5]), // reflexionsgrad
                Double.parseDouble(teile[start + 6]), // transparenz
                Double.parseDouble(teile[start + 7])  // brechungsindex
        );
    }

    private static void pruefeAnzahl(String[] teile, int erwartet, int zeilennummer) {
        if (teile.length != erwartet) {
            throw new IllegalArgumentException(
                    "Zeile " + zeilennummer + " erwartet " + erwartet +
                            " Werte, hat aber " + teile.length + "."
            );
        }
    }
}