package example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PetCareScheduler {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "hh:mm a";
    public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

    private enum AppointmentFilter {
        NONE,
        SHOW_UPCOMING,
        SHOW_PAST,

    }


    private Scanner scanner;
    private HashMap<String, Pet> pets;

    public PetCareScheduler() {
        scanner = new Scanner(System.in);
        pets = new HashMap<>();
    }

    public static void main(String[] args) {
        var petCareScheduler = new PetCareScheduler();
//        var testAppointments = new ArrayList<>(
//                List.of(
//                        new Appointment("visist", LocalDateTime.parse("2025-12-21 06:45 PM", DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), "sample note"),
//                        new Appointment("visist 2", LocalDateTime.parse("2025-12-23 06:45 PM", DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), "sample note 2")
//
//                )
//        );
//        petCareScheduler.pets.put("1", new Pet(
//                "1", "Max", "aspin", 2, "mark", "09265708010", LocalDate.parse("2025-12-21", DateTimeFormatter.ofPattern(DATE_FORMAT)), testAppointments));
        while (true) {
            petCareScheduler.loadFromFile();
            var menuPrompt = "=== Pet Care Scheduler ===" +
                    "\n1. Register a pet" +
                    "\n2. Schedule an appointment" +
                    "\n3. Store the details in a file" +
                    "\n4. Display details of pets and/or appointments" +
                    "\n5. Generate reports" +
                    "\n6. Exit";
//
            System.out.println(menuPrompt);
            var choice = petCareScheduler.getInput("Choose option: ");
            switch (choice) {
                case "1":
                    petCareScheduler.registerPet();
                    continue;
                case "2":
                    petCareScheduler.scheduleAnAppointment();
                    continue;

                case "3":
                    petCareScheduler.saveToFile();
                    continue;
                case "4":
                    petCareScheduler.displayRecordMenu();
                    continue;
                default:
                    System.out.println("Error: Invalid menu number");
            }
        }
    }

    private String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private void registerPet() {
        var id = getInput("Enter id: ");
        if (id.isEmpty()) {
            System.out.println("Error: Id is empty.");
            return;
        }

        var idExists = pets.containsKey(id);
        if (idExists) {
            System.out.println("Error: id already exists.");
            return;
        }

        var name = getInput("Enter pet name: ");

        var breed = getInput("Enter breed: ");

        var ageInput = getInput("Enter age: ");
        int age;
        try {
            age = Integer.parseInt(ageInput);
            if (age < 1) {
                System.out.println("Error: Age cannot be less than 1.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: unable to parse age.");
            return;
        }

        var owner = getInput("Enter owner name: ");

        var contactInfo = getInput("Enter contact info: ");

        var registrationDateInput = getInput(String.format("Enter registration date (format: %s): ", DATE_FORMAT));
        LocalDate registrationDate;
        try {
            registrationDate = LocalDate.parse(registrationDateInput, DateTimeFormatter.ofPattern(DATE_FORMAT));
        } catch (DateTimeParseException e) {
            System.out.println("Error: Unable to parse date.");
            return;
        }

        var newPet = new Pet(id, name, breed, age, owner, contactInfo, registrationDate);
        System.out.println("New pet added: " + newPet);

        pets.put(id, newPet);
        System.out.println("Pet successfully added to pets.");
    }


    public void scheduleAnAppointment() {
        displayPets(false, AppointmentFilter.NONE);
        var petId = getInput("Enter Pet ID to schedule an appointment with: ");
        if (!pets.containsKey(petId)) {
            System.out.println("Error: Pet Id not found.");
            return;
        }

        var appointmentType = getInput("Enter appointment type (vet visit, vaccination, grooming): ");

        var dateTimeInput = getInput(String.format("Enter date and time (format %s):", DATE_TIME_FORMAT));
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(dateTimeInput, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        } catch (DateTimeParseException e) {
            System.out.println("Error: Unable to parse date time format.");
            return;
        }

        var notes = getInput("Enter notes: ");

        var pet = pets.get(petId);
        var appointment = new Appointment(appointmentType, dateTime, notes);
        pet.getAppointments().add(appointment);
        System.out.println("Successfully created new appointment: " + appointment);
    }


    public void displayRecordMenu() {
        var displayOptions = """
                === Display Options ===\
                
                1. All registered pets\
                
                2. All appointments for a specific pet\
                
                3. Upcoming appointments for all pets\
                
                4. Past appointment history for each pet\
                
                5. Go back to main menu""";

        var running = true;
        while (running) {
            System.out.println(displayOptions);
            var choice = getInput("Choose an options: ");
            switch (choice) {
                case "1":
                    displayPets(false, AppointmentFilter.NONE);
                    continue;
                case "2":
                    displayPetAppointments();
                    continue;
                case "3":
                    displayUpcomingAppointments();
                    continue;
                case "4":
                    displayPets(true, AppointmentFilter.SHOW_PAST);
                    continue;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Error: Invalid option");
            }

        }
    }

    public void displayPetAppointments() {
        displayPets(false, AppointmentFilter.NONE);
        var id = getInput("Enter id: ");
        if (id.isEmpty()) {
            System.out.println("Error: Id is empty.");
            return;
        }

        var idExists = pets.containsKey(id);
        if (!idExists) {
            System.out.println("Error: id does not exists.");
            return;
        }
        var pet = pets.get(id);

        printPet(pet, true, AppointmentFilter.NONE);
    }

    public void displayPets(boolean showAppointment, AppointmentFilter appointmentFilter) {
        if (pets.isEmpty()) {
            System.out.println("No pets/appointments to display");
            return;
        }
        System.out.println("=== Pet List ===");
        pets.forEach((id, pet) -> {
            printPet(pet, showAppointment, appointmentFilter);
        });
        System.out.println("=== end of list ===");
    }

    public void displayUpcomingAppointments() {
        displayPets(true, AppointmentFilter.SHOW_UPCOMING);
    }


    private void printPet(Pet pet, boolean showAppointment, AppointmentFilter appointmentFilter) {
        var printString = String.format("Id: %s, Name: %s, Breed: %s, Age: %s, Owner: %s, Contact Info: %s, Registration Date: %s", pet.getId(), pet.getName(), pet.getBreed(), pet.getAge(), pet.getOwnerName(), pet.getContactInfo(), pet.getRegistrationDate());
        var border = "";
        for (int i = 0; i < printString.length(); i++) {
            border = border.concat("*");
        }
        System.out.println(border);
        System.out.println(printString);
        if (showAppointment) {
            System.out.println("Appointments:");
            if (!pet.getAppointments().isEmpty()) {

                switch (appointmentFilter) {

                    case AppointmentFilter.SHOW_UPCOMING:
                        var showUpcomingAppointments = pet.getAppointments().stream().filter(a -> a.getDateTime().isAfter(LocalDateTime.now())).toList();
                        showUpcomingAppointments.forEach(a -> {
                            System.out.printf("- Appointment Type: %s, Date/Time: %s, Notes: %s\n", a.getAppointmentType(), a.getDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), a.getNotes() == null || a.getNotes().isEmpty() ? "N/A" : a.getNotes());

                        });
                        break;
                    case AppointmentFilter.SHOW_PAST:
                        var showPastAppointments = pet.getAppointments().stream().filter(a -> a.getDateTime().isBefore(LocalDateTime.now())).toList();
                        showPastAppointments.forEach(a -> {
                            System.out.printf("- Appointment Type: %s, Date/Time: %s, Notes: %s\n", a.getAppointmentType(), a.getDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), a.getNotes() == null || a.getNotes().isEmpty() ? "N/A" : a.getNotes());

                        });
                        break;
                    default:

                        pet.getAppointments().forEach(appointment -> {
                            System.out.printf("- Appointment Type: %s, Date/Time: %s, Notes: %s\n", appointment.getAppointmentType(), appointment.getDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), appointment.getNotes() == null || appointment.getNotes().isEmpty() ? "N/A" : appointment.getNotes());
                        });

                }
//                if (appointmentFilter.equals(A)) {
//                    var showUpcomingAppointments = pet.getAppointments().stream().filter(a -> a.getDateTime().isAfter(LocalDateTime.now())).toList();
//                    showUpcomingAppointments.forEach(a -> {
//                        System.out.printf("- Appointment Type: %s, Date/Time: %s, Notes: %s\n", a.getAppointmentType(), a.getDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), a.getNotes() == null || a.getNotes().isEmpty() ? "N/A" : a.getNotes());
//
//                    });
//                } else {
//                    pet.getAppointments().forEach(appointment -> {
//                        System.out.printf("- Appointment Type: %s, Date/Time: %s, Notes: %s\n", appointment.getAppointmentType(), appointment.getDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), appointment.getNotes() == null || appointment.getNotes().isEmpty() ? "N/A" : appointment.getNotes());
//                    });
//                }

            } else {
                System.out.println("(No appointments yet.)");
            }
        }


    }

    private void saveToFile() {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream((new FileOutputStream("pets.ser")))) {
            objectOutputStream.writeObject(pets);
            System.out.println("Pets successfully saved.");
        } catch (IOException e) {
            System.out.println("Error: Unable to write to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        Path path = Paths.get("pets.ser");
        if (!Files.exists(path)) {
            return;
        }

        try (ObjectInputStream objectInputStream = new ObjectInputStream((new FileInputStream("pets.ser")))) {
            pets = (HashMap<String, Pet>) objectInputStream.readObject();
            System.out.println("Pets successfully saved.");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Class not found.");
        } catch (IOException e) {
            System.out.println("Error: File not found.");
        }
    }
}
