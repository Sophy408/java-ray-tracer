import java.util.List;

public interface Objekt {
    Treffer schneide(Strahl strahl);
    List<Treffer> findeTreffer(Strahl strahl);
}

//egal welches objekt, man muss dem Lichtstrahl rufen, ob er getroffen hat