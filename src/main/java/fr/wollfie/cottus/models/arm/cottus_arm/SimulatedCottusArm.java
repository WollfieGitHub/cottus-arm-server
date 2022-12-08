package fr.wollfie.cottus.models.arm.cottus_arm;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Articulation;

import java.util.List;

/**
 * The simulated cottus arm, its movement speed is infinite and 
 * its position is controlled live by the user.
 */
public class SimulatedCottusArm implements CottusArm {
    
    @Override
    public List<Articulation> getArticulations() {
        return null;
    }
}
