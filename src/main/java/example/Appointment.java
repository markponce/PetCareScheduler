package example;

import java.io.Serializable;
import java.time.LocalDateTime;


public class Appointment implements Serializable {
    public Appointment(String appointmentType, LocalDateTime dateTime, String notes) {
        this.appointmentType = appointmentType;
        this.dateTime = dateTime;
        this.notes = notes;
    }

    private String appointmentType;
    private LocalDateTime dateTime;
    private String notes;


    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentType='" + appointmentType + '\'' +
                ", dateTime=" + dateTime +
                ", notes='" + notes + '\'' +
                '}';
    }
}
