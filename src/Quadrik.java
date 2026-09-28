import java.util.ArrayList;
import java.util.List;

//eine schlaue Knete die durch Mathe alles sein kann
public class Quadrik implements Objekt {
    double A, B, C, D, E, F, G, H, I, J;
    Matrix4 transformation;
    Matrix4 inverseTransformation;
    Material material;

    Quadrik(double A, double B, double C, double D, double E, double F,
            double G, double H, double I, double J,
            Matrix4 transformation, Matrix4 inverseTransformation,
            Material material) {

        this.A = A;
        this.B = B;
        this.C = C;
        this.D = D;
        this.E = E;
        this.F = F;
        this.G = G;
        this.H = H;
        this.I = I;
        this.J = J;
        this.transformation = transformation;
        this.inverseTransformation = inverseTransformation;
        this.material = material;
    }

    public Treffer schneide(Strahl strahl) {
        List<Treffer> treffer = findeTreffer(strahl);

        if (treffer.isEmpty()) {
            return null;
        }

        return treffer.get(0);
    }

    public List<Treffer> findeTreffer(Strahl strahl) {
        List<Treffer> trefferListe = new ArrayList<>();

        Vektor3 o = inverseTransformation.transformierePunkt(strahl.ursprung);
        Vektor3 d = inverseTransformation.transformiereRichtung(strahl.richtung).normiere();

        double ox = o.x;
        double oy = o.y;
        double oz = o.z;

        double dx = d.x;
        double dy = d.y;
        double dz = d.z;

        double a =
                A * dx * dx +
                        B * dy * dy +
                        C * dz * dz +
                        D * dx * dy +
                        E * dx * dz +
                        F * dy * dz;

        double b =
                2 * A * ox * dx +
                        2 * B * oy * dy +
                        2 * C * oz * dz +
                        D * (ox * dy + oy * dx) +
                        E * (ox * dz + oz * dx) +
                        F * (oy * dz + oz * dy) +
                        G * dx +
                        H * dy +
                        I * dz;

        double c =
                A * ox * ox +
                        B * oy * oy +
                        C * oz * oz +
                        D * ox * oy +
                        E * ox * oz +
                        F * oy * oz +
                        G * ox +
                        H * oy +
                        I * oz +
                        J;

        double diskriminante = b * b - 4 * a * c;

        if (diskriminante < 0) {
            return trefferListe;
        }

        double wurzel = Math.sqrt(diskriminante);

        double t1 = (-b - wurzel) / (2.0 * a);
        double t2 = (-b + wurzel) / (2.0 * a);

        if (t1 > 0.001) {
            trefferListe.add(erstelleTreffer(strahl, o, d, t1, true));
        }

        if (t2 > 0.001) {
            trefferListe.add(erstelleTreffer(strahl, o, d, t2, false));
        }

        return trefferListe;
    }

    private Treffer erstelleTreffer(Strahl originalStrahl, Vektor3 o, Vektor3 d, double t, boolean eintritt) {
        Vektor3 lokalerPunkt = o.plus(d.mal(t));

        Vektor3 lokaleNormale = new Vektor3(
                2 * A * lokalerPunkt.x + D * lokalerPunkt.y + E * lokalerPunkt.z + G,
                2 * B * lokalerPunkt.y + D * lokalerPunkt.x + F * lokalerPunkt.z + H,
                2 * C * lokalerPunkt.z + E * lokalerPunkt.x + F * lokalerPunkt.y + I
        ).normiere();

        if (!eintritt) {
            lokaleNormale = lokaleNormale.mal(-1);
        }

        Vektor3 weltPunkt = transformation.transformierePunkt(lokalerPunkt);
        Vektor3 weltNormale = transformation.transformiereRichtung(lokaleNormale).normiere();

        double entfernung = weltPunkt.minus(originalStrahl.ursprung).laenge();

        return new Treffer(entfernung, weltPunkt, weltNormale, material, eintritt, this);
    }
}