package fr.wollfie.cottus.models.arm.positioning.kinematics;

import fr.wollfie.cottus.utils.maths.matrices.HTMatrix;
import fr.wollfie.cottus.utils.maths.matrices.Matrix;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache the transformation matrix, so we don't have to recompute each one of them
 */
public class CachedDHTable extends DHTable{

//=========   ====  == =
//      CACHE
//=========   ====  == =
    
    /** Given from < to, possible transformations are as follows :
     * 
     * <pre>
     *     from/to| 1 | 2 | ... | size |
     *          1 |   |   |     |      |
     *          2 | X |   |     |      |
     *         ...| X | X |     |      |
     *        size| X | X | X   |      |
     * </pre>
     *   
     * Which mean a correct indexing of the cache can be : {@code from+to*size}
     * */
    private final Map<Integer, HTMatrix> cachedTransforms = new ConcurrentHashMap<>();

    private int getIndexFor(int from, int to) { return from+to*size(); }
    
//=========   ====  == =
//      CONSTRUCTOR
//=========   ====  == =
    
    public CachedDHTable(int size, double[] d, double[] theta, double[] a, double[] alpha) {
        super(d, a, theta, alpha);
    }

//=========   ====  == =
//      CACHE MIDDLEWARE LOGIC
//=========   ====  == =
    
    /**
     * Cache the result of the transform for further requests
     * @param from The index of the articulation with the source space
     * @param to The index of the articulation with the destination space
     * @return The cached transform if it exists or the computation of the super class otherwise
     */
    @Override
    public HTMatrix getTransformMatrix(int from, int to) {
        int index = getIndexFor(from, to);
        if (cachedTransforms.containsKey(index)) { return cachedTransforms.get(index); }

        HTMatrix result = super.getTransformMatrix(from, to);
        cachedTransforms.put(index, result);
        return result;
    }
}
