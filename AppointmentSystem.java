
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppointmentSystem {

    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Appointment> appointments = new ArrayList<>();
    private static final Path DATA_FILE = Path.of("appointments.txt");
    private static int nextId = 1;

    static class Appointment {
        int id;
        String patientName;
        int age;
        String date;
        String time;
        String reason;
        boolean cancelled;

        Appointment(int id, String patientName, int age,
                    String date, String time, String reason,
                    boolean cancelled) {
            this.id = id;
            this.patientName = patientName;
            this.age = age;
            this.date = date;
            this.time = time;
            this.reason = reason;
            this.cancelled = cancelled;
        }

        void display() {
            System.out.println("\nAppointment ID: " + id);
            System.out.println("Patient: " + patientName);
            System.out.println("Age: " + age);
            System.out.println("Date: " + date);
            System.out.println("Time: " + time);
            System.out.println("Reason: " + reason);
            System.out.println("Status: "
                    + (cancelled ? "Cancelled" : "Scheduled"));
        }
    }

    public static void main(String[] args) {
        loadAppointments();

        System.out.println("================================");
        System.out.println(" PATIENT APPOINTMENT SYSTEM");
        System.out.println("================================");

        boolean running = true;

        while (running) {
            System.out.println("\n1. Add appointment");
            System.out.println("2. View appointments");
            System.out.println("3. Search by patient name");
            System.out.println("4. Cancel appointment");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addAppointment();
                case "2" -> viewAppointments();
                case "3" -> searchAppointments();
                case "4" -> cancelAppointment();
                case "5" -> {
                    saveAppointments();
                    System.out.println("Appointments saved. Goodbye!");
                    running = false;
                }
                default -> System.out.println(
                        "Invalid choice. Enter a number from 1 to 5.");
            }
        }

        scanner.close();
    }

    private static void addAppointment() {
        System.out.println("\n--- Add Appointment ---");

        String name = readRequired("Patient name: ");

        int age;
        while (true) {
            System.out.print("Patient age: ");
            try {
                age = Integer.parseInt(scanner.nextLine().trim());
                if (age >= 0 && age <= 120) {
                    break;
                }
            } catch (NumberFormatException ignored) {
                // Ask again when the input is not a number.
            }
            System.out.println("Enter a valid age from 0 to 120.");
        }

        String date;
        while (true) {
            System.out.print("Appointment date (YYYY-MM-DD): ");
            date = scanner.nextLine().trim();
            try {
                LocalDate parsedDate = LocalDate.parse(date);
                if (!parsedDate.isBefore(LocalDate.now())) {
                    break;
                }
                System.out.println("Choose today or a future date.");
            } catch (DateTimeParseException e) {
                System.out.println("Use the format YYYY-MM-DD.");
            }
        }

        String time;
        while (true) {
            System.out.print("Appointment time (HH:MM, 24-hour): ");
            time = scanner.nextLine().trim();
            try {
                LocalTime.parse(time);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Use a valid time, such as 14:30.");
            }
        }

        String reason = readRequired("Appointment reason: ");

        Appointment appointment = new Appointment(
                nextId++, name, age, date, time, reason, false);

        appointments.add(appointment);
        saveAppointments();

        System.out.println("Appointment added successfully!");
        System.out.println("Your appointment ID is: " + appointment.id);
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                // Keep each record on one line in the data file.
                return value.replace('\t', ' ')
                            .replace('\n', ' ')
                            .replace('\r', ' ');
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private static void viewAppointments() {
        System.out.println("\n--- All Appointments ---");

        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }

        for (Appointment appointment : appointments) {
            appointment.display();
        }
    }

    private static void searchAppointments() {
        System.out.print("\nEnter patient name to search: ");
        String query = scanner.nextLine().trim();

        boolean found = false;

        for (Appointment appointment : appointments) {
            if (appointment.patientName
                    .toLowerCase()
                    .contains(query.toLowerCase())) {
                appointment.display();
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching appointments found.");
        }
    }

    private static void cancelAppointment() {
        System.out.print("\nEnter appointment ID to cancel: ");

        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid numeric ID.");
            return;
        }

        for (Appointment appointment : appointments) {
            if (appointment.id == id) {
                if (appointment.cancelled) {
                    System.out.println("This appointment is already cancelled.");
                } else {
                    appointment.cancelled = true;
                    saveAppointments();
                    System.out.println("Appointment cancelled successfully.");
                }
                return;
            }
        }

        System.out.println("Appointment ID not found.");
    }

    private static void saveAppointments() {
        List<String> lines = new ArrayList<>();

        for (Appointment a : appointments) {
            lines.add(a.id + "\t" + a.patientName + "\t" + a.age
                    + "\t" + a.date + "\t" + a.time + "\t"
                    + a.reason + "\t" + a.cancelled);
        }

        try {
            Files.write(DATA_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not save appointments: "
                    + e.getMessage());
        }
    }

    private static void loadAppointments() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(
                    DATA_FILE, StandardCharsets.UTF_8);

            for (String line : lines) {
                String[] fields = line.split("\t", -1);

                if (fields.length != 7) {
                    continue;
                }

                try {
                    Appointment a = new Appointment(
                            Integer.parseInt(fields[0]),
                            fields[1],
                            Integer.parseInt(fields[2]),
                            fields[3],
                            fields[4],
                            fields[5],
                            Boolean.parseBoolean(fields[6]));

                    appointments.add(a);
                    nextId = Math.max(nextId, a.id + 1);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping an invalid saved record.");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load saved appointments: "
                    + e.getMessage());
        }
    }
}
