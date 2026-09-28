import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CSGObjekt implements Objekt {
    private final Objekt objektA;
    private final Objekt objektB;
    private final CSGOperation operation;

    public CSGObjekt(Objekt objektA, Objekt objektB, CSGOperation operation) {
        this.objektA = objektA;
        this.objektB = objektB;
        this.operation = operation;
    }

    @Override
    public Treffer schneide(Strahl strahl) {
        List<Treffer> treffer = findeTreffer(strahl);
        return treffer.isEmpty() ? null : treffer.get(0);
    }

    @Override
    public List<Treffer> findeTreffer(Strahl strahl) {
        List<Treffer> hitsA = objektA.findeTreffer(strahl);
        List<Treffer> hitsB = objektB.findeTreffer(strahl);

        List<Treffer> allHits = new ArrayList<>();
        allHits.addAll(hitsA);
        allHits.addAll(hitsB);

        // Sortiere alle Treffer lückenlos von nah nach fern
        allHits.sort(Comparator.comparingDouble(h -> h.entfernung));

        List<Treffer> ergebnis = new ArrayList<>();
        boolean inA = false;
        boolean inB = false;

        for (Treffer hit : allHits) {
            boolean vonA = istTeilVon(hit.objekt, objektA);
            boolean altInCSG = evaluiereZustand(inA, inB);

            // Zustand aktualisieren
            if (vonA) {
                inA = hit.eintritt;
            } else {
                inB = hit.eintritt;
            }

            boolean neuInCSG = evaluiereZustand(inA, inB);

            // Wenn sich der Zustand ändert haben wir eine echte Oberfläche getroffen
            if (altInCSG != neuInCSG) {
                hit.eintritt = neuInCSG;
                ergebnis.add(hit);
            }
        }
        return ergebnis;
    }

    private boolean istTeilVon(Objekt getroffen, Objekt ziel) {
        if (getroffen == ziel) return true;
        if (ziel instanceof CSGObjekt) {
            CSGObjekt csg = (CSGObjekt) ziel;
            return istTeilVon(getroffen, csg.objektA) || istTeilVon(getroffen, csg.objektB);
        }
        return false;
    }

    private boolean evaluiereZustand(boolean inA, boolean inB) {
        switch (operation) {
            case VEREINIGUNG: return inA || inB; //nimmt zwei Knetkugeln und klebt sie zu einer großen Figur zusammen
            case SCHNITT:     return inA && inB; //behält nur das Innere wo sich beide Kugeln mitten drin kreuzen
            case DIFFERENZ:   return inA && !inB; //nimmt eine Kugel und stanzt mit der anderen ein Loch hinein
            default:          return false;
        }
    }
}