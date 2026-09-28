class Material {
    Vektor3 farbe;
    double rauheit;
    double metallisch;

    // 0 = nicht spiegelnd, 1 = perfekter Spiegelanteil
    double reflexionsgrad;

    // 0 = undurchsichtig, 1 = komplett transparent
    double transparenz;

    // Luft ca. 1.0, Wasser ca. 1.33, Glas ca. 1.5, Diamant ca. 2.42
    double brechungsindex;

    Material(Vektor3 farbe, double rauheit, double metallisch,
             double reflexionsgrad, double transparenz, double brechungsindex) {
        this.farbe = farbe;
        this.rauheit = begrenzen(rauheit, 0.02, 1.0);
        this.metallisch = begrenzen(metallisch, 0.0, 1.0);
        this.reflexionsgrad = begrenzen(reflexionsgrad, 0.0, 1.0);
        this.transparenz = begrenzen(transparenz, 0.0, 1.0);
        this.brechungsindex = Math.max(1.0, brechungsindex);
    }

    private double begrenzen(double wert, double min, double max) {
        return Math.max(min, Math.min(max, wert));
    }
}