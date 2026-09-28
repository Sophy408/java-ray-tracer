public class Treffer {
    public double entfernung;
    public Vektor3 position;
    public Vektor3 normale;
    public Material material;
    public boolean eintritt;
    public Objekt objekt;

    public Treffer(double entfernung, Vektor3 position, Vektor3 normale,
                   Material material, boolean eintritt, Objekt objekt) {
        this.entfernung = entfernung;
        this.position = position;
        this.normale = normale;
        this.material = material;
        this.eintritt = eintritt;
        this.objekt = objekt;
    }
}