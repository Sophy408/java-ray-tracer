import java.util.ArrayList;
import java.util.List;

public class Szene {
    int breite = 800;
    int hoehe = 600;
    int maxTiefe = 5;
    String ausgabeDatei = "594568.png";

    Kamera kamera = new Kamera(
            new Vektor3(0, 0, 0),
            new Vektor3(0, 0, -1),
            60
    );

    Licht licht = new Licht(new Vektor3(2, 5, -2), 3.0);
    Vektor3 hintergrund = new Vektor3(0.1, 0.1, 0.15);
    List<Objekt> objekte = new ArrayList<>();
}