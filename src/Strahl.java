class Strahl {
    Vektor3 ursprung;
    Vektor3 richtung;

    Strahl(Vektor3 ursprung, Vektor3 richtung) {
        this.ursprung = ursprung;

        // Die Richtung wird normiert, damit Entfernungen später sauber berechnet werden
        this.richtung = richtung.normiere();
    }
}