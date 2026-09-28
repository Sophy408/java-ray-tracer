class Vektor3 {
    double x, y, z;

    Vektor3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vektor3 plus(Vektor3 v) {
        return new Vektor3(x + v.x, y + v.y, z + v.z);
    }

    Vektor3 minus(Vektor3 v) {
        return new Vektor3(x - v.x, y - v.y, z - v.z);
    }

    Vektor3 mal(double faktor) {
        return new Vektor3(x * faktor, y * faktor, z * faktor);
    }

    // Komponentenweise Multiplikation für farbige Licht oder Materialanteile
    Vektor3 mal(Vektor3 v) {
        return new Vektor3(x * v.x, y * v.y, z * v.z);
    }

    Vektor3 geteiltDurch(double faktor) {
        return new Vektor3(x / faktor, y / faktor, z / faktor);
    }

    Vektor3 negiert() {
        return new Vektor3(-x, -y, -z);
    }

    double skalarprodukt(Vektor3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    Vektor3 kreuzprodukt(Vektor3 v) {
        return new Vektor3(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x
        );
    }

    double laenge() {
        return Math.sqrt(skalarprodukt(this));
    }

    Vektor3 normiere() {
        double laenge = laenge();
        if (laenge == 0.0) {
            return new Vektor3(0, 0, 0);
        }
        return new Vektor3(x / laenge, y / laenge, z / laenge);
    }

    Vektor3 begrenze(double min, double max) {
        return new Vektor3(
                Math.max(min, Math.min(max, x)),
                Math.max(min, Math.min(max, y)),
                Math.max(min, Math.min(max, z))
        );
    }
}