package unl.edu.cc.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Especialidad implements Serializable {

    // Atributos
    private String nombre;
    private String descripcion;

    // Relación UML (una especialidad puede tener varios médicos)
    private List<Medico> medicos;

    // Constructor vacío
    public Especialidad() {
        this.medicos = new ArrayList<>();
    }

    // Constructor con parámetros
    public Especialidad(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.medicos = new ArrayList<>();
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Medico> getMedicos() {
        return medicos;
    }

    // Método para agregar médico a la especialidad
    public void agregarMedico(Medico medico) {

        if (medico != null) {
            medicos.add(medico);
            System.out.println("Médico agregado a la especialidad correctamente.");
        } else {
            System.out.println("No se pudo agregar el médico.");
        }
    }

    // Mostrar información
    @Override
    public String toString() {
        return "Especialidad: " +
                "\nNombre=" + nombre +
                "\nDescripción='" + descripcion +
                "\nTotal de médicos=" + medicos.size() +
                '}';
    }
}