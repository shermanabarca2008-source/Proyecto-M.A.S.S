package unl.edu.cc.view;

import unl.edu.cc.domain.*;

import java.io.File;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String DATA_FILE = "admin_store.dat";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Administrador admin = new Administrador(1, "admin123");
        // Intentar cargar datos si existe archivo
        File f = new File(DATA_FILE);
        if (f.exists()) {
            Administrador cargado = Administrador.cargarUltimoAdministrador();
            if (cargado != null) {
                admin = cargado;
            }
        }

        List<Paciente> pacientes = new ArrayList<>();

        System.out.println("===== SISTEMA MASS DE AGENDAMIENTO DE CITAS MÉDICAS =====");
        while (true) {
            System.out.println("\nSeleccione una opción:");
            System.out.println("1) Registrarse");
            System.out.println("2) Iniciar sesión");
            System.out.println("3) Salir");
            System.out.println("4) Administrador");
            System.out.print("> ");
            String opcion = scanner.nextLine().trim();
            if ("1".equals(opcion)) {
                registrarPaciente(scanner, pacientes);
            } else if ("2".equals(opcion)) {
                Paciente paciente = iniciarSesion(scanner, pacientes);
                if (paciente != null) {
                    ejecutarPanelPaciente(scanner, paciente, admin);
                }
            } else if ("3".equals(opcion)) {
                System.out.println("Gracias por usar MASS. Hasta luego.");
                break;
            } else if ("4".equals(opcion)) {
                // Acceso de administrador
                if (adminLogin(scanner, admin)) {
                    admin = ejecutarPanelAdmin(scanner, admin);
                } else {
                    System.out.println("Credenciales de administrador incorrectas.");
                }
            } else {
                System.out.println("Opción inválida. Intente nuevamente.");
            }
        }

        scanner.close();
    }


    private static void registrarPaciente(Scanner scanner, List<Paciente> pacientes) {
        System.out.println("\n===== REGISTRO DE PACIENTE =====");
        System.out.print("Cédula: ");
        String cedula = scanner.nextLine().trim();
        if (!validarCedula(cedula)) {
            System.out.println("Cédula inválida. Debe tener 10 dígitos numéricos.");
            return;
        }
        if (buscarPacientePorCedula(pacientes, cedula) != null) {
            System.out.println("Ya existe un paciente registrado con esa cédula.");
            return;
        }
        System.out.print("Nombre completo: ");
        String nombre = scanner.nextLine().trim();
        System.out.print("Fecha de nacimiento (yyyy-MM-dd): ");
        String fechaNacimientoTexto = scanner.nextLine().trim();
        LocalDate fechaNacimiento;
        try {
            fechaNacimiento = LocalDate.parse(fechaNacimientoTexto, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            System.out.println("Formato de fecha incorrecto.");
            return;
        }
        System.out.print("Correo electrónico: ");
        String correo = scanner.nextLine().trim();
        System.out.print("Número de contacto: ");
        String telefono = scanner.nextLine().trim();
        System.out.print("Contraseña: ");
        String contrasena = scanner.nextLine().trim();

        Paciente paciente = new Paciente(cedula, nombre, correo, contrasena, fechaNacimiento, telefono);
        pacientes.add(paciente);
        System.out.println("Registro exitoso. Ya puede iniciar sesión.");
    }

    private static Paciente iniciarSesion(Scanner scanner, List<Paciente> pacientes) {
        System.out.println("\n===== INICIO DE SESIÓN =====");
        System.out.print("Cédula: ");
        String cedula = scanner.nextLine().trim();
        System.out.print("Contraseña: ");
        String contrasena = scanner.nextLine().trim();
        Paciente paciente = buscarPacientePorCedula(pacientes, cedula);
        if (paciente == null || !contrasena.equals(paciente.getContrasena())) {
            System.out.println("Cédula o contraseña incorrecta.");
            return null;
        }
        System.out.println("Bienvenido " + paciente.getNombreCompleto() + ".");
        return paciente;
    }

    private static void ejecutarPanelPaciente(Scanner scanner, Paciente paciente, Administrador admin) {
        while (true) {
            System.out.println("\n===== PANEL DE GESTIÓN DE CITAS =====");
            System.out.println("1) Agendar cita");
            System.out.println("2) Cancelar cita");
            System.out.println("3) Reagendar cita");
            System.out.println("4) Ver historial de citas e indicaciones");
            System.out.println("5) Cerrar sesión");
            System.out.print("> ");
            String opcion = scanner.nextLine().trim();
            switch (opcion) {
                case "1":
                    agendarCita(scanner, paciente, admin);
                    break;
                case "2":
                    cancelarCita(scanner, paciente);
                    break;
                case "3":
                    reagendarCita(scanner, paciente, admin);
                    break;
                case "4":
                    mostrarHistorial(paciente);
                    break;
                case "5":
                    System.out.println("Cierre de sesión exitoso.");
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void agendarCita(Scanner scanner, Paciente paciente, Administrador admin) {
        System.out.println("\n===== AGENDAR CITA =====");
        if (admin.getEspecialidades().isEmpty()) {
            System.out.println("No hay especialidades disponibles.");
            return;
        }
        if (admin.getMedicos().isEmpty()) {
            System.out.println("Medicos aún no disponibles.");
            return;
        }
        mostrarEspecialidades(admin.getEspecialidades());
        System.out.print("Seleccione el número de especialidad: ");
        int indice = leerEntero(scanner, 1, admin.getEspecialidades().size()) - 1;
        Especialidad especialidad = admin.getEspecialidades().get(indice);

        List<Medico> medicos = especialidad.getMedicos();
        if (medicos.isEmpty()) {
            System.out.println("No hay médicos asignados a esa especialidad.");
            return;
        }
        System.out.println("\nMédicos disponibles para " + especialidad.getNombre() + ":");
        for (int i = 0; i < medicos.size(); i++) {
            Medico medicoListado = medicos.get(i);
            System.out.printf("%d) Médico: %s - Especialidad: %s%n", i + 1,
                    medicoListado.getNombreCompleto(),
                    medicoListado.getEspecialidad() != null ? medicoListado.getEspecialidad().getNombre() : "Sin especialidad");
        }
        System.out.print("Seleccione el número de médico: ");
        Medico medico = medicos.get(leerEntero(scanner, 1, medicos.size()) - 1);

        List<HorarioMedico> horariosDisponibles = buscarHorariosDisponibles(medico);
        if (horariosDisponibles.isEmpty()) {
            System.out.println("No hay horarios disponibles para ese médico.");
            return;
        }
        mostrarHorariosDisponibles(horariosDisponibles);
        System.out.print("Seleccione el número de horario: ");
        HorarioMedico horario = horariosDisponibles.get(leerEntero(scanner, 1, horariosDisponibles.size()) - 1);

        Cita cita = crearCitaDesdeHorario(paciente, medico, horario);
        boolean agregado = paciente.agendarCita(cita);
        if (agregado) {
            horario.reservarHorario();
            medico.agregarCita(cita);
            Notificacion notificacion = new Notificacion("Su cita ha sido agendada correctamente.", new Date(), cita);
            cita.setNotificacion(notificacion);
            notificacion.enviarNotificacion();
        } else {
            System.out.println("No fue posible agendar la cita.");
        }
    }

    private static Cita crearCitaDesdeHorario(Paciente paciente, Medico medico, HorarioMedico horario) {
        Date fecha = Date.from(horario.getFechaDisponible().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Time hora = Time.valueOf(horario.getHoraInicio());
        Cita cita = new Cita(fecha, hora, medico, paciente);
        cita.agendar();
        return cita;
    }

    private static void cancelarCita(Scanner scanner, Paciente paciente) {
        System.out.println("\n===== CANCELAR CITA =====");
        List<Cita> citasActivas = new ArrayList<>();
        for (Cita cita : paciente.getCitas()) {
            if (cita.getEstado() == Cita.EstadoCita.AGENDADA || cita.getEstado() == Cita.EstadoCita.REAGENDADA) {
                citasActivas.add(cita);
            }
        }
        if (citasActivas.isEmpty()) {
            System.out.println("No hay citas activas para cancelar.");
            return;
        }
        mostrarCitas(citasActivas);
        System.out.print("Seleccione el número de cita a cancelar: ");
        Cita citaSeleccionada = citasActivas.get(leerEntero(scanner, 1, citasActivas.size()) - 1);
        if (!puedeCancelar(citaSeleccionada)) {
            System.out.println("No es posible cancelar la cita con la antelación requerida.");
            return;
        }
        citaSeleccionada.cancelar();
        if (citaSeleccionada.getNotificacion() != null) {
            citaSeleccionada.getNotificacion().enviarNotificacion();
        }
    }

    private static void reagendarCita(Scanner scanner, Paciente paciente, Administrador admin) {
        System.out.println("\n===== REAGENDAR CITA =====");
        List<Cita> citasActivas = new ArrayList<>();
        for (Cita cita : paciente.getCitas()) {
            if (cita.getEstado() == Cita.EstadoCita.AGENDADA || cita.getEstado() == Cita.EstadoCita.REAGENDADA) {
                citasActivas.add(cita);
            }
        }
        if (citasActivas.isEmpty()) {
            System.out.println("No hay citas activas que puedan reagendarse.");
            return;
        }
        mostrarCitas(citasActivas);
        System.out.print("Seleccione el número de cita a reagendar: ");
        Cita citaSeleccionada = citasActivas.get(leerEntero(scanner, 1, citasActivas.size()) - 1);

        Medico medico = citaSeleccionada.getMedico();
        System.out.println("Reagendando con el mismo médico: " + medico.getNombreCompleto());

        List<HorarioMedico> horariosDisponibles = buscarHorariosDisponibles(medico);
        if (horariosDisponibles.isEmpty()) {
            System.out.println("No hay horarios disponibles para ese médico, elija otra especialidad.");
            mostrarEspecialidades(admin.getEspecialidades());
            System.out.print("Seleccione el número de especialidad: ");
            Especialidad especialidad = admin.getEspecialidades().get(leerEntero(scanner, 1, admin.getEspecialidades().size()) - 1);
            if (especialidad.getMedicos().isEmpty()) {
                System.out.println("No hay médicos en esta especialidad.");
                return;
            }
            System.out.println("\nMédicos disponibles para " + especialidad.getNombre() + ":");
            for (int i = 0; i < especialidad.getMedicos().size(); i++) {
                Medico medicoListado = especialidad.getMedicos().get(i);
                System.out.printf("%d) Médico: %s - Especialidad: %s%n", i + 1,
                        medicoListado.getNombreCompleto(),
                        medicoListado.getEspecialidad() != null ? medicoListado.getEspecialidad().getNombre() : "Sin especialidad");
            }
            System.out.print("Seleccione el número de médico: ");
            medico = especialidad.getMedicos().get(leerEntero(scanner, 1, especialidad.getMedicos().size()) - 1);
            horariosDisponibles = buscarHorariosDisponibles(medico);
            if (horariosDisponibles.isEmpty()) {
                System.out.println("No hay horarios disponibles para ese médico.");
                return;
            }
        }
        mostrarHorariosDisponibles(horariosDisponibles);
        System.out.print("Seleccione el número de nuevo horario: ");
        HorarioMedico nuevoHorario = horariosDisponibles.get(leerEntero(scanner, 1, horariosDisponibles.size()) - 1);
        nuevoHorario.reservarHorario();

        citaSeleccionada.reagendar(Date.from(nuevoHorario.getFechaDisponible().atStartOfDay(ZoneId.systemDefault()).toInstant()), Time.valueOf(nuevoHorario.getHoraInicio()));
        citaSeleccionada.getMedico().getCitas().remove(citaSeleccionada);
        medico.agregarCita(citaSeleccionada);
        citaSeleccionada.setNotificacion(new Notificacion("Su cita ha sido reagendada correctamente.", new Date(), citaSeleccionada));
        citaSeleccionada.getNotificacion().enviarNotificacion();
    }

    private static void mostrarHistorial(Paciente paciente) {
        System.out.println("\n===== HISTORIAL DE CITAS =====");
        if (paciente.getCitas().isEmpty()) {
            System.out.println("No hay citas registradas.");
            return;
        }
        for (Cita cita : paciente.getCitas()) {
            System.out.println(cita);
            if (!cita.getIndicaciones().isEmpty()) {
                System.out.println("Indicaciones asociadas:");
                for (IndicacionesMedicas indicacion : cita.getIndicaciones()) {
                    System.out.println(" - " + indicacion);
                }
            }
        }
    }

    private static void mostrarEspecialidades(List<Especialidad> especialidades) {
        for (int i = 0; i < especialidades.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, especialidades.get(i).getNombre());
        }
    }

    private static void mostrarHorariosDisponibles(List<HorarioMedico> horarios) {
        System.out.println("\nHorarios disponibles:");
        for (int i = 0; i < horarios.size(); i++) {
            HorarioMedico horario = horarios.get(i);
            String fecha = horario.getFechaDisponible().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String inicio = horario.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm"));
            String fin = horario.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm"));
            System.out.printf("%d) %s %s - %s%n", i + 1, fecha, inicio, fin);
        }
    }

    private static void mostrarCitas(List<Cita> citas) {
        for (int i = 0; i < citas.size(); i++) {
            System.out.printf("%d)\n%s%n", i + 1, citas.get(i));
        }
    }

    private static Administrador ejecutarPanelAdmin(Scanner scanner, Administrador admin) {
        while (true) {
            System.out.println("\n===== PANEL ADMINISTRADOR =====");
            System.out.println("1) Agregar especialidad");
            System.out.println("2) Modificar especialidad");
            System.out.println("3) Eliminar especialidad");
            System.out.println("4) Agregar médico");
            System.out.println("5) Modificar médico");
            System.out.println("6) Eliminar médico");
            System.out.println("7) Agregar horario a médico");
            System.out.println("8) Ver especialidades y médicos");
            System.out.println("9) Generar slots automáticos (30 min)");
            System.out.println("10) Ver datos guardados en archivo");
            System.out.println("11) Guardar datos actuales");
            System.out.println("12) Cargar datos desde archivo");
            System.out.println("13) Salir panel administrador");
            System.out.print("> ");
            String opc = scanner.nextLine().trim();
            switch (opc) {
                case "1":
                    agregarEspecialidad(scanner, admin);
                    break;
                case "2":
                    modificarEspecialidad(scanner, admin);
                    break;
                case "3":
                    eliminarEspecialidad(scanner, admin);
                    break;
                case "4":
                    agregarMedico(scanner, admin);
                    break;
                case "5":
                    modificarMedico(scanner, admin);
                    break;
                case "6":
                    eliminarMedico(scanner, admin);
                    break;
                case "7":
                    agregarHorario(scanner, admin);
                    break;
                case "8":
                    System.out.println("\nEspecialidades registradas:");
                    for (Especialidad e : admin.getEspecialidades()) {
                        System.out.println("- " + e.getNombre() + " (" + e.getMedicos().size() + " médicos)");
                    }
                    System.out.println("\nMédicos registrados:");
                    mostrarMedicos(admin.getMedicos());
                    break;
                case "9":
                    adminGenerarSlots(scanner, admin);
                    break;
                case "10":
                    mostrarDatosArchivados();
                    break;
                case "11":
                    admin.guardarEnArchivo();
                    break;
                case "12":
                    Administrador cargado = Administrador.cargarUltimoAdministrador();
                    if (cargado != null) {
                        admin = cargado;
                        System.out.println("Administrador actualizado con datos cargados desde el archivo.");
                    } else {
                        System.out.println("No se pudo cargar datos.");
                    }
                    break;
                case "13":
                    return admin;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void agregarEspecialidad(Scanner scanner, Administrador admin) {
        System.out.println("\n===== AGREGAR ESPECIALIDAD =====");
        String cont = "SI";
        while (cont.equalsIgnoreCase("SI")) {
            System.out.print("Nombre de la especialidad: ");
            String nombre = scanner.nextLine().trim();
            System.out.print("Descripción: ");
            String desc = scanner.nextLine().trim();
            Especialidad esp = new Especialidad(nombre, desc);
            admin.gestionarEspecialidad(esp);
            System.out.println("Especialidad agregada.");
            System.out.print("Desea seguir ingresando Especialidades? (SI/NO): ");
            cont = scanner.nextLine().trim();
        }
    }

    private static void modificarEspecialidad(Scanner scanner, Administrador admin) {
        System.out.println("\n===== MODIFICAR ESPECIALIDAD =====");
        if (admin.getEspecialidades().isEmpty()) {
            System.out.println("No hay especialidades registradas.");
            return;
        }

        String cont = "SI";
        while (cont.equalsIgnoreCase("SI")) {
            mostrarEspecialidades(admin.getEspecialidades());
            System.out.print("Seleccione número de especialidad a modificar: ");
            int idx = leerEntero(scanner, 1, admin.getEspecialidades().size()) - 1;
            Especialidad esp = admin.getEspecialidades().get(idx);

            String cont2 = "SI";
            while (cont2.equalsIgnoreCase("SI")) {
                System.out.println("Ingrese la característica a modificar: \n1.Nombre \n2.Descripción\n");
                int car;
                try {
                    car = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida.");
                    continue;
                }
                switch (car) {
                    case 1:
                        System.out.print("Nuevo nombre: ");
                        esp.setNombre(scanner.nextLine().trim());
                        System.out.println("Nombre actualizado.");
                        break;
                    case 2:
                        System.out.print("Nueva descripción: ");
                        esp.setDescripcion(scanner.nextLine().trim());
                        System.out.println("Descripción actualizada.");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
                System.out.print("¿Desea cambiar otra característica de esta especialidad? (SI/NO): ");
                cont2 = scanner.nextLine().trim();
            }

            System.out.print("¿Desea modificar otra especialidad? (SI/NO): ");
            cont = scanner.nextLine().trim();
        }
    }

    private static void eliminarEspecialidad(Scanner scanner, Administrador admin) {
        System.out.println("\n===== ELIMINAR ESPECIALIDAD =====");
        if (admin.getEspecialidades().isEmpty()) {
            System.out.println("No hay especialidades registradas.");
            return;
        }
        mostrarEspecialidades(admin.getEspecialidades());
        System.out.print("Seleccione número de especialidad a eliminar: ");
        int idx = leerEntero(scanner, 1, admin.getEspecialidades().size()) - 1;
        System.out.print("Confirma eliminar? (s/n): ");
        String conf = scanner.nextLine().trim().toLowerCase();
        if ("s".equals(conf)) {
            admin.eliminarEspecialidad(admin.getEspecialidades().get(idx));
        } else {
            System.out.println("Eliminación cancelada.");
        }
    }

    private static void agregarMedico(Scanner scanner, Administrador admin) {
        System.out.println("\n===== AGREGAR MÉDICO =====");
        if (admin.getEspecialidades().isEmpty()) {
            System.out.println("No hay especialidades registradas. Cree una especialidad primero.");
            return;
        }

        String cont = "SI";
        while (cont.equalsIgnoreCase("SI")) {
            System.out.print("Nombre completo del médico: ");
            String nombre = scanner.nextLine().trim();

            System.out.println("Seleccione especialidad (0 para crear nueva):");
            mostrarEspecialidades(admin.getEspecialidades());
            int sel = leerEntero(scanner, 0, admin.getEspecialidades().size());
            Especialidad especialidad;
            if (sel == 0) {
                System.out.print("Nombre de la nueva especialidad: ");
                String nombreEsp = scanner.nextLine().trim();
                System.out.print("Descripción de la especialidad: ");
                String descEsp = scanner.nextLine().trim();
                especialidad = new Especialidad(nombreEsp, descEsp);
                admin.gestionarEspecialidad(especialidad);
            } else {
                especialidad = admin.getEspecialidades().get(sel - 1);
            }

            Medico medico = new Medico(nombre, especialidad);
            admin.gestionarMedico(medico);
            especialidad.agregarMedico(medico);
            System.out.println("Médico agregado exitosamente.");

            String contHorario = "SI";
            while (contHorario.equalsIgnoreCase("SI")) {
                System.out.print("Fecha disponible (yyyy-MM-dd): ");
                String fechaTxt = scanner.nextLine().trim();
                LocalDate fecha;
                try {
                    fecha = LocalDate.parse(fechaTxt, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    System.out.println("Formato de fecha inválido.");
                    continue;
                }
                System.out.print("Hora inicio (HH:mm): ");
                String hi = scanner.nextLine().trim();
                System.out.print("Hora fin (HH:mm): ");
                String hf = scanner.nextLine().trim();
                LocalTime horaInicio;
                LocalTime horaFin;
                try {
                    horaInicio = LocalTime.parse(hi);
                    horaFin = LocalTime.parse(hf);
                } catch (Exception e) {
                    System.out.println("Formato de hora inválido.");
                    continue;
                }
                LocalTime apertura = LocalTime.of(8, 0);
                LocalTime cierre = LocalTime.of(17, 30);
                long minutos = ChronoUnit.MINUTES.between(horaInicio, horaFin);
                if (horaInicio.isBefore(apertura) || horaFin.isAfter(cierre) || minutos != 30) {
                    System.out.println("El horario debe estar entre 08:00 y 17:30 y durar exactamente 30 minutos.");
                    continue;
                }
                HorarioMedico horario = new HorarioMedico(fecha, horaInicio, horaFin, true);
                admin.agregarHorarioAMedico(medico, horario);
                System.out.println("Horario agregado al médico.");
                System.out.print("Desea ingresar otro horario para este médico? (SI/NO): ");
                contHorario = scanner.nextLine().trim();
            }

            System.out.print("Desea seguir ingresando Médicos? (SI/NO): ");
            cont = scanner.nextLine().trim();
        }
    }

    private static void modificarMedico(Scanner scanner, Administrador admin) {
        System.out.println("\n===== MODIFICAR MÉDICO =====");
        if (admin.getMedicos().isEmpty()) {
            System.out.println("No hay médicos registrados.");
            return;
        }

        String cont = "SI";
        while (cont.equalsIgnoreCase("SI")) {
            mostrarMedicos(admin.getMedicos());
            System.out.print("Seleccione número de médico a modificar: ");
            int idx = leerEntero(scanner, 1, admin.getMedicos().size()) - 1;
            Medico medico = admin.getMedicos().get(idx);

            String cont2 = "SI";
            while (cont2.equalsIgnoreCase("SI")) {
                System.out.println("Ingrese la característica a modificar: \n1.Nombre \n2.Especialidad \n3.Horarios\n");
                int car;
                try {
                    car = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida.");
                    continue;
                }
                switch (car) {
                    case 1:
                        System.out.print("Nuevo nombre: ");
                        medico.setNombreCompleto(scanner.nextLine().trim());
                        System.out.println("Nombre actualizado.");
                        break;
                    case 2:
                        if (admin.getEspecialidades().isEmpty()) {
                            System.out.println("No hay especialidades registradas.");
                        } else {
                            System.out.println("Seleccione nueva especialidad (0 para crear nueva):");
                            mostrarEspecialidades(admin.getEspecialidades());
                            int sel = leerEntero(scanner, 0, admin.getEspecialidades().size());
                            Especialidad nuevaEsp;
                            if (sel == 0) {
                                System.out.print("Nombre de la nueva especialidad: ");
                                String nombreEsp = scanner.nextLine().trim();
                                System.out.print("Descripción de la especialidad: ");
                                String descEsp = scanner.nextLine().trim();
                                nuevaEsp = new Especialidad(nombreEsp, descEsp);
                                admin.gestionarEspecialidad(nuevaEsp);
                            } else {
                                nuevaEsp = admin.getEspecialidades().get(sel - 1);
                            }
                            if (medico.getEspecialidad() != null && medico.getEspecialidad() != nuevaEsp) {
                                medico.getEspecialidad().getMedicos().remove(medico);
                            }
                            medico.setEspecialidad(nuevaEsp);
                            nuevaEsp.agregarMedico(medico);
                            System.out.println("Especialidad actualizada.");
                        }
                        break;
                    case 3:
                        if (medico.getHorarios().isEmpty()) {
                            System.out.println("El médico no tiene horarios registrados.");
                        }
                        System.out.println("1) Agregar horario\n2) Modificar horario existente\n3) Eliminar horario");
                        int horaAccion = leerEntero(scanner, 1, 3);
                        switch (horaAccion) {
                            case 1:
                                System.out.print("Fecha disponible (yyyy-MM-dd): ");
                                String fechaTxt = scanner.nextLine().trim();
                                LocalDate fecha;
                                try {
                                    fecha = LocalDate.parse(fechaTxt, DateTimeFormatter.ISO_LOCAL_DATE);
                                } catch (Exception e) {
                                    System.out.println("Formato de fecha inválido.");
                                    break;
                                }
                                System.out.print("Hora inicio (HH:mm): ");
                                String hi = scanner.nextLine().trim();
                                System.out.print("Hora fin (HH:mm): ");
                                String hf = scanner.nextLine().trim();
                                LocalTime horaInicio;
                                LocalTime horaFin;
                                try {
                                    horaInicio = LocalTime.parse(hi);
                                    horaFin = LocalTime.parse(hf);
                                } catch (Exception e) {
                                    System.out.println("Formato de hora inválido.");
                                    break;
                                }
                                LocalTime apertura = LocalTime.of(8, 0);
                                LocalTime cierre = LocalTime.of(17, 30);
                                long minutos = ChronoUnit.MINUTES.between(horaInicio, horaFin);
                                if (horaInicio.isBefore(apertura) || horaFin.isAfter(cierre) || minutos != 30) {
                                    System.out.println("El horario debe estar entre 08:00 y 17:30 y durar exactamente 30 minutos.");
                                    break;
                                }
                                HorarioMedico horarioNuevo = new HorarioMedico(fecha, horaInicio, horaFin, true);
                                admin.agregarHorarioAMedico(medico, horarioNuevo);
                                System.out.println("Horario agregado.");
                                break;
                            case 2:
                                if (medico.getHorarios().isEmpty()) {
                                    System.out.println("No hay horarios para modificar.");
                                    break;
                                }
                                for (int i = 0; i < medico.getHorarios().size(); i++) {
                                    HorarioMedico h = medico.getHorarios().get(i);
                                    System.out.printf("%d) %s %s - %s (%s)%n", i + 1,
                                            h.getFechaDisponible().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                            h.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")),
                                            h.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")),
                                            h.isDisponible() ? "disponible" : "ocupado");
                                }
                                System.out.print("Seleccione horario a modificar: ");
                                int horarioIdx = leerEntero(scanner, 1, medico.getHorarios().size()) - 1;
                                HorarioMedico horarioModificar = medico.getHorarios().get(horarioIdx);
                                System.out.print("Nueva fecha (yyyy-MM-dd): ");
                                String fechaNuevoTxt = scanner.nextLine().trim();
                                System.out.print("Nueva hora inicio (HH:mm): ");
                                String hiNuevo = scanner.nextLine().trim();
                                System.out.print("Nueva hora fin (HH:mm): ");
                                String hfNuevo = scanner.nextLine().trim();
                                try {
                                    LocalDate fechaNueva = LocalDate.parse(fechaNuevoTxt, DateTimeFormatter.ISO_LOCAL_DATE);
                                    LocalTime horaInicioNueva = LocalTime.parse(hiNuevo);
                                    LocalTime horaFinNueva = LocalTime.parse(hfNuevo);
                                    long minutosNuevos = ChronoUnit.MINUTES.between(horaInicioNueva, horaFinNueva);
                                    if (horaInicioNueva.isBefore(LocalTime.of(8, 0)) || horaFinNueva.isAfter(LocalTime.of(17, 30)) || minutosNuevos != 30) {
                                        System.out.println("El horario debe estar entre 08:00 y 17:30 y durar exactamente 30 minutos.");
                                    } else {
                                        horarioModificar.setFechaDisponible(LocalDate.parse(fechaNuevoTxt, DateTimeFormatter.ISO_LOCAL_DATE));
                                        horarioModificar.setHoraInicio(LocalTime.parse(hiNuevo));
                                        horarioModificar.setHoraFin(LocalTime.parse(hfNuevo));
                                        System.out.println("Horario modificado.");
                                    }
                                } catch (Exception e) {
                                    System.out.println("Formato de fecha/hora inválido.");
                                }
                                break;
                            case 3:
                                if (medico.getHorarios().isEmpty()) {
                                    System.out.println("No hay horarios para eliminar.");
                                    break;
                                }
                                for (int i = 0; i < medico.getHorarios().size(); i++) {
                                    HorarioMedico h = medico.getHorarios().get(i);
                                    System.out.printf("%d) %s %s - %s (%s)%n", i + 1,
                                            h.getFechaDisponible().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                            h.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")),
                                            h.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")),
                                            h.isDisponible() ? "disponible" : "ocupado");
                                }
                                System.out.print("Seleccione horario a eliminar: ");
                                int eliminarIdx = leerEntero(scanner, 1, medico.getHorarios().size()) - 1;
                                medico.getHorarios().remove(eliminarIdx);
                                System.out.println("Horario eliminado.");
                                break;
                        }
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
                System.out.print("¿Desea cambiar otra característica de este médico? (SI/NO): ");
                cont2 = scanner.nextLine().trim();
            }

            System.out.print("¿Desea modificar otro médico? (SI/NO): ");
            cont = scanner.nextLine().trim();
        }
    }

    private static void eliminarMedico(Scanner scanner, Administrador admin) {
        System.out.println("\n===== ELIMINAR MÉDICO =====");
        if (admin.getMedicos().isEmpty()) {
            System.out.println("No hay médicos registrados.");
            return;
        }
        mostrarMedicos(admin.getMedicos());
        System.out.print("Seleccione número de médico a eliminar: ");
        int idx = leerEntero(scanner, 1, admin.getMedicos().size()) - 1;
        System.out.print("Confirma eliminar? (s/n): ");
        String conf = scanner.nextLine().trim().toLowerCase();
        if ("s".equals(conf)) {
            admin.eliminarMedico(admin.getMedicos().get(idx));
        } else {
            System.out.println("Eliminación cancelada.");
        }
    }

    private static void agregarHorario(Scanner scanner, Administrador admin) {
        System.out.println("\n===== AGREGAR HORARIO A MÉDICO =====");
        if (admin.getMedicos().isEmpty()) {
            System.out.println("No hay médicos registrados.");
            return;
        }

        String cont = "SI";
        while (cont.equalsIgnoreCase("SI")) {
            mostrarMedicos(admin.getMedicos());
            System.out.print("Seleccione médico: ");
            Medico medico = admin.getMedicos().get(leerEntero(scanner, 1, admin.getMedicos().size()) - 1);

            System.out.print("Fecha disponible (yyyy-MM-dd): ");
            String fechaTxt = scanner.nextLine().trim();
            LocalDate fecha;
            try {
                fecha = LocalDate.parse(fechaTxt, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                System.out.println("Formato de fecha inválido.");
                continue;
            }
            System.out.print("Hora inicio (HH:mm): ");
            String hi = scanner.nextLine().trim();
            System.out.print("Hora fin (HH:mm): ");
            String hf = scanner.nextLine().trim();
            LocalTime horaInicio, horaFin;
            try {
                horaInicio = LocalTime.parse(hi);
                horaFin = LocalTime.parse(hf);
            } catch (Exception e) {
                System.out.println("Formato de hora inválido.");
                continue;
            }

            // Validar ventana laboral: 08:00 - 17:30
            LocalTime apertura = LocalTime.of(8, 0);
            LocalTime cierre = LocalTime.of(17, 30);
            long minutos = ChronoUnit.MINUTES.between(horaInicio, horaFin);
            if (horaInicio.isBefore(apertura) || horaFin.isAfter(cierre) || minutos != 30) {
                System.out.println("El horario debe estar entre 08:00 y 17:30 y durar exactamente 30 minutos.");
                continue;
            }

            HorarioMedico horario = new HorarioMedico(fecha, horaInicio, horaFin, true);
            admin.agregarHorarioAMedico(medico, horario);
            System.out.println("Horario agregado.");
            System.out.print("Desea ingresar otro horario? (SI/NO): ");
            cont = scanner.nextLine().trim();
        }
    }

    private static void adminGenerarSlots(Scanner scanner, Administrador admin) {
        System.out.println("\n===== GENERAR SLOTS AUTOMÁTICOS (30 min) =====");
        if (admin.getMedicos().isEmpty()) {
            System.out.println("No hay médicos registrados.");
            return;
        }
        System.out.println("Seleccione médico:");
        mostrarMedicos(admin.getMedicos());
        Medico med = admin.getMedicos().get(leerEntero(scanner, 1, admin.getMedicos().size()) - 1);
        System.out.print("Fecha inicio (yyyy-MM-dd): ");
        String inicioTxt = scanner.nextLine().trim();
        System.out.print("Fecha fin (yyyy-MM-dd) (enter = misma fecha): ");
        String finTxt = scanner.nextLine().trim();
        LocalDate inicio;
        LocalDate fin;
        try {
            inicio = LocalDate.parse(inicioTxt, DateTimeFormatter.ISO_LOCAL_DATE);
            if (finTxt.isEmpty()) {
                fin = inicio;
            } else {
                fin = LocalDate.parse(finTxt, DateTimeFormatter.ISO_LOCAL_DATE);
            }
        } catch (Exception e) {
            System.out.println("Formato de fecha inválido.");
            return;
        }
        if (fin.isBefore(inicio)) {
            System.out.println("La fecha fin no puede ser anterior a la fecha inicio.");
            return;
        }

        int agregados = 0;
        for (LocalDate d = inicio; !d.isAfter(fin); d = d.plusDays(1)) {
            LocalTime t = LocalTime.of(8, 0);
            LocalTime cierre = LocalTime.of(17, 30);
            while (!t.plusMinutes(30).isAfter(cierre)) {
                LocalTime finSlot = t.plusMinutes(30);
                // comprobar duplicados por fecha y hora inicio
                boolean existe = false;
                for (HorarioMedico h : med.getHorarios()) {
                    if (h.getFechaDisponible().equals(d) && h.getHoraInicio().equals(t)) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    HorarioMedico nuevo = new HorarioMedico(d, t, finSlot, true);
                    admin.agregarHorarioAMedico(med, nuevo);
                    agregados++;
                }
                t = t.plusMinutes(30);
            }
        }
        System.out.println("Proceso finalizado. Horarios agregados: " + agregados);
    }

    private static void mostrarDatosArchivados() {
        System.out.println("\n===== DATOS GUARDADOS EN EL ARCHIVO =====");
        List<Administrador> lista = Administrador.cargarDesdeArchivo();
        if (lista.isEmpty()) {
            System.out.println("No hay datos guardados en el archivo.");
            return;
        }
        int numero = 1;
        for (Administrador admin : lista) {
            System.out.println("\nSnapshot #" + numero++ + ":");
            System.out.println("Administrador id=" + admin.getIdAdmin());
            System.out.println("Especialidades:");
            for (Especialidad esp : admin.getEspecialidades()) {
                System.out.println("- " + esp.getNombre() + " (" + esp.getMedicos().size() + " médicos)");
            }
            System.out.println("Médicos:");
            for (Medico med : admin.getMedicos()) {
                System.out.println("- Médico: " + med.getNombreCompleto());
                System.out.println("  Especialidad: " +
                        (med.getEspecialidad() != null ? med.getEspecialidad().getNombre() : "Sin especialidad"));
                if (!med.getHorarios().isEmpty()) {
                    System.out.println("  Horarios:");
                    for (HorarioMedico h : med.getHorarios()) {
                        System.out.printf("   * %s %s - %s (%s)%n",
                                h.getFechaDisponible().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                h.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")),
                                h.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")),
                                h.isDisponible() ? "disponible" : "ocupado");
                    }
                } else {
                    System.out.println("  Horarios: ninguno");
                }
            }
        }
    }

    // ADMINISTRADOR: login y panel
    private static boolean adminLogin(Scanner scanner, Administrador admin) {
        System.out.println("\n===== LOGIN ADMINISTRADOR =====");
        System.out.print("ID administrador: ");
        String idTxt = scanner.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(idTxt);
        } catch (NumberFormatException e) {
            return false;
        }
        System.out.print("Contraseña: ");
        String pwd = scanner.nextLine().trim();
        return id == admin.getIdAdmin() && pwd.equals(admin.getContrasena());
    }


    private static void mostrarMedicos(List<Medico> medicos) {
        for (int i = 0; i < medicos.size(); i++) {
            Medico medico = medicos.get(i);
            System.out.println((i + 1) + ") Médico: " + medico.getNombreCompleto());
            System.out.println("   Especialidad: " +
                    (medico.getEspecialidad() != null ? medico.getEspecialidad().getNombre() : "Sin especialidad"));
            if (medico.getHorarios().isEmpty()) {
                System.out.println("   Horarios: ninguno");
            } else {
                System.out.println("   Horarios:");
                for (HorarioMedico horario : medico.getHorarios()) {
                    System.out.printf("      - %s %s - %s (%s)%n",
                            horario.getFechaDisponible().format(DateTimeFormatter.ISO_LOCAL_DATE),
                            horario.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")),
                            horario.getHoraFin().format(DateTimeFormatter.ofPattern("HH:mm")),
                            horario.isDisponible() ? "disponible" : "ocupado");
                }
            }
        }
    }

    private static List<HorarioMedico> buscarHorariosDisponibles(Medico medico) {
        List<HorarioMedico> disponibles = new ArrayList<>();
        for (HorarioMedico horario : medico.getHorarios()) {
            if (horario.isDisponible()) {
                disponibles.add(horario);
            }
        }
        return disponibles;
    }

    private static Paciente buscarPacientePorCedula(List<Paciente> pacientes, String cedula) {
        for (Paciente paciente : pacientes) {
            if (paciente.getCedula().equals(cedula)) {
                return paciente;
            }
        }
        return null;
    }

    private static boolean validarCedula(String cedula) {
        return cedula != null && cedula.matches("\\d{10}");
    }

    private static int leerEntero(Scanner scanner, int min, int max) {
        while (true) {
            try {
                int valor = Integer.parseInt(scanner.nextLine().trim());
                if (valor >= min && valor <= max) {
                    return valor;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.print("Ingrese un número válido entre " + min + " y " + max + ": ");
        }
    }

    private static boolean puedeCancelar(Cita cita) {
        LocalDateTime fechaHora = convertirAFechaHora(cita.getFecha(), cita.getHora());
        LocalDateTime ahora = LocalDateTime.now();
        return ChronoUnit.HOURS.between(ahora, fechaHora) >= 24;
    }

    private static LocalDateTime convertirAFechaHora(Date fecha, Time hora) {
        return LocalDateTime.ofInstant(fecha.toInstant(), ZoneId.systemDefault())
                .withHour(hora.toLocalTime().getHour())
                .withMinute(hora.toLocalTime().getMinute())
                .withSecond(hora.toLocalTime().getSecond());
    }
}
