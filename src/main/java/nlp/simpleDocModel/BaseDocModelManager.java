package nlp.simpleDocModel;

import algebra.DistanceMatrix;
import nlp.utils.Simplex;
import numeric.Distance;

import java.util.ArrayList;
import java.util.Set;

/**
 * The type Base doc model manager.
 *
 * @param <L> the type parameter
 */
public class BaseDocModelManager<L extends BaseDocumentModel> {
    /**
     * The Doc models.
     */
    protected ArrayList<L> docModels;
    /**
     * The Simplex.
     */
    protected Simplex simplex;

    /**
     * Instantiates a new Base doc model manager.
     */
    public BaseDocModelManager() {
        docModels = new ArrayList<>();
        simplex = new Simplex();
    }

    /**
     * Add void.
     *
     * @param l lowbow curve
     */
    public void add(L l) {
        docModels.add(l);

        Set<String> keys = l.getSimplex().getKeySet();
        int accIndex = simplex.size();
        for (String s : keys) {
            Integer aux = simplex.get(s);
            if (aux == null) {
                accIndex++;
                simplex.put(s, accIndex);
            }
        }
    }


    /**
     * Gets doc models.
     *
     * @return the doc models
     */
    public ArrayList<L> getDocModels() {
        return docModels;
    }


    /**
     * Remove all.
     */
    public void removeAll() {
        docModels.removeAll(docModels);
        simplex = new Simplex();
    }

    /**
     * Gets simplex.
     *
     * @return the simplex
     */
    public Simplex getSimplex() {
        return simplex;
    }

    /**
     * Sets simplex.
     *
     * @param simplex the simplex
     */
    public void setSimplex(Simplex simplex) {
        this.simplex = simplex;
        for (L docModel : docModels) {
            docModel.setSimplex(simplex);
        }
    }

    /**
     * Initializes all curves with the same vocabulary
     */
    public void build() {
        for (L docModel : docModels) {
            docModel.setSimplex(simplex);
            docModel.build();
        }
    }

    /**
     * Gets distance matrix.
     *
     * @param distance the distance function ( can be a similary function instead)
     * @return the distance matrix
     */
    public DistanceMatrix getDistanceMatrix(Distance<L> distance) {
        ArrayList<L> docModels = this.getDocModels();
        int size = docModels.size();
        DistanceMatrix distanceMatrix = new DistanceMatrix(size);
        for (int i = 2; i <= size; i++) {
            for (int j = 1; j < i; j++) {
                L lowbowJ = docModels.get(j - 1);
                L lowbowI = docModels.get(i - 1);
                distanceMatrix.setXY(i, j, distance.dist(lowbowI, lowbowJ));
            }
        }
        return distanceMatrix;
    }
}
