package fr.wollfie.cottus.services;

/** Any service which is capable of moving the arm. 
 * NB : Only one such service should have its {@link ArmControllerService#update()} method
 * called at a time, i.e., only one service should control the arm at a time*/
public interface ArmControllerService {

    /** Update the internal state of the animator */
    void update();
}
