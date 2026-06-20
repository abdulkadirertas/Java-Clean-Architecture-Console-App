package core.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

public class ScheduleResponseDTO {
    private final Map<LocalDateTime, Boolean> schedule;

    /**
     * Initializes the schedule response DTO object by copying the original schedule of the barber.
     * @param schedule The schedule of the barber.
     */
    public ScheduleResponseDTO(Map<LocalDateTime,Boolean> schedule){
        //Create a copy
        this.schedule = schedule != null ? new TreeMap<>(schedule) : new TreeMap<>();
    }

    /**
     * Returns the read-only copy of the barber's schedule.
     * @return An unmodifiable copy of the barber's schedule.
     */
    public Map<LocalDateTime, Boolean> getSchedule() {
        //Return a read only map
        return java.util.Collections.unmodifiableMap(schedule);
    }
}
