package core.application;

import core.dto.response.BarberResponseDTO;
import entity.user.barber.ApplicationState;
import entity.user.barber.Barber;
import infrastructure.user.IUserRepository;

import java.util.ArrayList;
import java.util.List;

public class ApplicationService {
    //dependencies
    private final IUserRepository<Barber> barberRepository;

    /**
     * Initializes the repository dependency
     * @param barberRepository The repository contract containing all barbers in the system.
     */
    public ApplicationService(IUserRepository<Barber> barberRepository){
        this.barberRepository = barberRepository;
    }

    /**
     * Filters the pending applications and returns a list of their DTO object.
     * @return A list containing pending barber DTO objects.
     */
    public List<BarberResponseDTO> getPendingApplications(){
        List<BarberResponseDTO> pendingApplications = new ArrayList<>();
        for(Barber barber: barberRepository.getAll()){
            if(barber.getApprovalStatus() == ApplicationState.PENDING){
                BarberResponseDTO barberDTO = new BarberResponseDTO(barber.getUserId(), barber.getFirstName(), barber.getLastName(), barber.getPhone(), barber.getShopName());
                pendingApplications.add(barberDTO);
            }
        }
        return pendingApplications;
    }

    /**
     * Updates the approval status of the barber.
     * @param id The barber ID whose status will be updated.
     * @param status New application status (e.g, APPROVED, REJECTED, PENDING).
     * @throws IllegalArgumentException if the related barber cannot be found in the system.
     */
    public void updateApprovalStatus(String id, ApplicationState status){
        Barber barber = barberRepository.get(id);
        if(barber == null) throw new IllegalArgumentException("Barber cannot be found in the system while changing the application status");

        barber.updateApprovalStatus(status);
        if(status == ApplicationState.APPROVED)barber.generateFreshSchedule();
        barberRepository.save();
    }
}
