public class Kamera {
    Vektor3 position;
    Vektor3 blickrichtung;
    double sichtfeld;

    private Vektor3 rechts;
    private Vektor3 oben;

    Kamera(Vektor3 position, Vektor3 blickrichtung, double sichtfeld) {
        this.position = position;
        this.blickrichtung = blickrichtung.normiere();
        this.sichtfeld = sichtfeld;

        // Stabile Kamerabasis: Dadurch wird die Blickrichtung aus der Szenendatei wirklich benutzt.
        Vektor3 weltOben = new Vektor3(0, 1, 0);
        if (Math.abs(this.blickrichtung.skalarprodukt(weltOben)) > 0.999) {
            weltOben = new Vektor3(1, 0, 0);
        }

        this.rechts = this.blickrichtung.kreuzprodukt(weltOben).normiere();
        this.oben = this.rechts.kreuzprodukt(this.blickrichtung).normiere();
        // Kamera kann frei ausgerichtet werden
    }

    Strahl erzeugeStrahl(int pixelX, int pixelY, int breite, int hoehe) {
        double seitenverhaeltnis = (double) breite / hoehe;
        double sichtfeldSkalierung = Math.tan(Math.toRadians(sichtfeld * 0.5));

        double bildschirmX = (2 * ((pixelX + 0.5) / breite) - 1) * seitenverhaeltnis * sichtfeldSkalierung;
        double bildschirmY = (1 - 2 * ((pixelY + 0.5) / hoehe)) * sichtfeldSkalierung;

        Vektor3 richtung = blickrichtung
                .plus(rechts.mal(bildschirmX))
                .plus(oben.mal(bildschirmY))
                .normiere();

        return new Strahl(position, richtung);
    }
}