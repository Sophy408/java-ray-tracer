import java.util.ArrayList;
import java.util.List;

class Kugel implements Objekt {
    Vektor3 mitte;
    double radius;
    Material material;

    Kugel(Vektor3 mitte, double radius, Material material) {
        this.mitte = mitte;
        this.radius = radius;
        this.material = material;
    }

    @Override
    public Treffer schneide(Strahl strahl) {
        List<Treffer> treffer = findeTreffer(strahl);
        return treffer.isEmpty() ? null : treffer.get(0);
    }

    @Override
    public List<Treffer> findeTreffer(Strahl strahl) {
        List<Treffer> trefferListe = new ArrayList<>();
        Vektor3 oc = strahl.ursprung.minus(mitte);

        double a = strahl.richtung.skalarprodukt(strahl.richtung);
        double b = 2.0 * oc.skalarprodukt(strahl.richtung);
        double c = oc.skalarprodukt(oc) - radius * radius;
        double diskriminante = b * b - 4 * a * c;

        if (diskriminante < 0) {
            return trefferListe;
        }

        double wurzel = Math.sqrt(diskriminante);
        double t1 = (-b - wurzel) / (2.0 * a);
        double t2 = (-b + wurzel) / (2.0 * a);

        if (t1 > 0.001) {
            Vektor3 p1 = strahl.ursprung.plus(strahl.richtung.mal(t1));
            Vektor3 n1 = p1.minus(mitte).normiere();
            trefferListe.add(new Treffer(t1, p1, n1, material, true, this));
        }

        if (t2 > 0.001) {
            Vektor3 p2 = strahl.ursprung.plus(strahl.richtung.mal(t2));
            Vektor3 n2 = p2.minus(mitte).normiere();
            // Wichtig für Cook-Torrance & CSG: Austritts-Normale umdrehen analog zur Quadrik
            n2 = n2.mal(-1);
            trefferListe.add(new Treffer(t2, p2, n2, material, false, this));
        }

        return trefferListe;
    }
}