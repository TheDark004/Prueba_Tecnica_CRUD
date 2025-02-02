/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package crudjava;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 *
 * @author sebas
 */
public class CRUDJava {

   private static final String CSV_FILE = "usuarios.csv";
    private static final String IMAGE_FOLDER = "fotos_usuarios/";
    private static DefaultTableModel model;

    public static void createCSVFile() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("ID,Nombre,Edad,Direccion,Telefono,Foto\n");
            } catch (IOException e) {
                System.out.println("Error al crear el archivo CSV: " + e.getMessage());
            }
        }
        // Crear la carpeta para las fotos si no existe
        new File(IMAGE_FOLDER).mkdir();
    }

    public static void crearUsuario(String nombre, int edad, String direccion, String telefono, String rutaImagen) {
        try (FileWriter writer = new FileWriter(CSV_FILE, true)) {
            int id = obtenerUltimoId() + 1;
            String nombreArchivoImagen = guardarImagen(id, rutaImagen);
            writer.write(id + "," + nombre + "," + edad + "," + direccion + "," + telefono + "," + nombreArchivoImagen + "\n");
        } catch (IOException e) {
            System.out.println("Error al crear usuario: " + e.getMessage());
        }
    }

    private static String guardarImagen(int id, String rutaImagen) {
        if (rutaImagen == null || rutaImagen.isEmpty()) {
            return ""; // No hay imagen
        }
        try {
            File archivoImagen = new File(rutaImagen);
            String nombreArchivo = "usuario_" + id + "_" + archivoImagen.getName();
            File destino = new File(IMAGE_FOLDER + nombreArchivo);
            BufferedImage imagen = ImageIO.read(archivoImagen);
            ImageIO.write(imagen, "jpg", destino);
            return nombreArchivo;
        } catch (IOException e) {
            System.out.println("Error al guardar la imagen: " + e.getMessage());
            return "";
        }
    }

    public static void eliminarUsuario(int id) {
        List<String[]> usuarios = obtenerUsuarios();
        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            writer.write("ID,Nombre,Edad,Direccion,Telefono,Foto\n");
            for (String[] usuario : usuarios) {
                if (Integer.parseInt(usuario[0]) != id) {
                    writer.write(String.join(",", usuario) + "\n");
                } else {
                    // Eliminar la imagen asociada al usuario
                    if (!usuario[5].isEmpty()) {
                        new File(IMAGE_FOLDER + usuario[5]).delete();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
        }
    }

    public static void modificarUsuario(int id, String nombre, int edad, String direccion, String telefono, String rutaImagen) {
        List<String[]> usuarios = obtenerUsuarios();
        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            writer.write("ID,Nombre,Edad,Direccion,Telefono,Foto\n");
            for (String[] usuario : usuarios) {
                if (Integer.parseInt(usuario[0]) == id) {
                    usuario[1] = nombre;
                    usuario[2] = String.valueOf(edad);
                    usuario[3] = direccion;
                    usuario[4] = telefono;
                    if (rutaImagen != null && !rutaImagen.isEmpty()) {
                        // Eliminar la imagen anterior si existe
                        if (!usuario[5].isEmpty()) {
                            new File(IMAGE_FOLDER + usuario[5]).delete();
                        }
                        // Guardar la nueva imagen
                        usuario[5] = guardarImagen(id, rutaImagen);
                    }
                }
                writer.write(String.join(",", usuario) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error al modificar usuario: " + e.getMessage());
        }
    }

    public static List<String[]> obtenerUsuarios() {
        List<String[]> usuarios = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            reader.readLine(); // Saltar la cabecera
            while ((line = reader.readLine()) != null) {
                usuarios.add(line.split(","));
            }
        } catch (IOException e) {
            System.out.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    private static int obtenerUltimoId() {
        List<String[]> usuarios = obtenerUsuarios();
        if (usuarios.isEmpty()) {
            return 0;
        }
        String[] ultimoUsuario = usuarios.get(usuarios.size() - 1);
        return Integer.parseInt(ultimoUsuario[0]);
    }

    public static void crearInterfaz() {
        JFrame frame = new JFrame("Gestión de Usuarios");
        frame.setSize(800, 600);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Crear Usuario", crearPanelCrearUsuario());
        tabbedPane.addTab("Lista de Usuarios", crearPanelListaUsuarios());
        frame.add(tabbedPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static JPanel crearPanelCrearUsuario() {
        JPanel panel = new JPanel(new GridLayout(6, 2));
        JTextField txtNombre = new JTextField(), txtEdad = new JTextField(), txtDireccion = new JTextField(), txtTelefono = new JTextField();
        JButton btnImagen = new JButton("Seleccionar Imagen"), btnCrear = new JButton("Crear Usuario");
        final String[] rutaImagen = {null};

        btnImagen.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                rutaImagen[0] = fileChooser.getSelectedFile().getAbsolutePath();
            }
        });

        btnCrear.addActionListener(e -> {
            try {
                crearUsuario(txtNombre.getText(), Integer.parseInt(txtEdad.getText()), txtDireccion.getText(), txtTelefono.getText(), rutaImagen[0]);
                JOptionPane.showMessageDialog(panel, "Usuario creado correctamente.");
                // Limpiar las casillas después de crear el usuario
                txtNombre.setText("");
                txtEdad.setText("");
                txtDireccion.setText("");
                txtTelefono.setText("");
                rutaImagen[0] = null;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Ingrese valores válidos.");
            }
        });

        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Edad:"));
        panel.add(txtEdad);
        panel.add(new JLabel("Dirección:"));
        panel.add(txtDireccion);
        panel.add(new JLabel("Teléfono:"));
        panel.add(txtTelefono);
        panel.add(new JLabel("Foto:"));
        panel.add(btnImagen);
        panel.add(new JLabel());
        panel.add(btnCrear);
        return panel;
    }

    public static JPanel crearPanelListaUsuarios() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] columnNames = {"ID", "Nombre", "Edad", "Dirección", "Teléfono", "Foto"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 5 ? Icon.class : String.class;
            }
        };
        cargarDatosEnTabla(model);
        JTable table = new JTable(model);
        table.setRowHeight(100); // Ajustar altura de fila para mostrar imágenes
        JScrollPane scrollPane = new JScrollPane(table);
        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        JButton btnModificar = new JButton("Modificar Seleccionado");
        JButton btnCargar = new JButton("Cargar Datos");

        btnEliminar.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = Integer.parseInt((String) model.getValueAt(selectedRow, 0));
                eliminarUsuario(id);
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(panel, "Usuario eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(panel, "Seleccione un usuario para eliminar.");
            }
        });

        btnModificar.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int id = Integer.parseInt((String) model.getValueAt(selectedRow, 0));
                String nombre = (String) model.getValueAt(selectedRow, 1);
                int edad = Integer.parseInt((String) model.getValueAt(selectedRow, 2));
                String direccion = (String) model.getValueAt(selectedRow, 3);
                String telefono = (String) model.getValueAt(selectedRow, 4);
                String foto = (String) model.getValueAt(selectedRow, 5);

                JPanel panelModificar = new JPanel(new GridLayout(6, 2));
                JTextField txtNombre = new JTextField(nombre);
                JTextField txtEdad = new JTextField(String.valueOf(edad));
                JTextField txtDireccion = new JTextField(direccion);
                JTextField txtTelefono = new JTextField(telefono);
                JButton btnImagen = new JButton("Seleccionar Nueva Imagen");
                final String[] nuevaRutaImagen = {null};

                btnImagen.addActionListener(ev -> {
                    JFileChooser fileChooser = new JFileChooser();
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        nuevaRutaImagen[0] = fileChooser.getSelectedFile().getAbsolutePath();
                    }
                });

                JButton btnGuardar = new JButton("Guardar Cambios");

                btnGuardar.addActionListener(ev -> {
                    try {
                        modificarUsuario(id, txtNombre.getText(), Integer.parseInt(txtEdad.getText()), txtDireccion.getText(), txtTelefono.getText(), nuevaRutaImagen[0]);
                        cargarDatosEnTabla(model); // Recargar datos en la tabla
                        JOptionPane.showMessageDialog(panel, "Usuario modificado correctamente.");
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(panel, "Ingrese valores válidos.");
                    }
                });

                panelModificar.add(new JLabel("Nombre:"));
                panelModificar.add(txtNombre);
                panelModificar.add(new JLabel("Edad:"));
                panelModificar.add(txtEdad);
                panelModificar.add(new JLabel("Dirección:"));
                panelModificar.add(txtDireccion);
                panelModificar.add(new JLabel("Teléfono:"));
                panelModificar.add(txtTelefono);
                panelModificar.add(new JLabel("Foto:"));
                panelModificar.add(btnImagen);
                panelModificar.add(new JLabel());
                panelModificar.add(btnGuardar);

                JOptionPane.showMessageDialog(null, panelModificar, "Modificar Usuario", JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "Seleccione un usuario para modificar.");
            }
        });

        btnCargar.addActionListener(e -> {
            cargarDatosEnTabla(model);
            JOptionPane.showMessageDialog(panel, "Datos cargados correctamente.");
        });

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnEliminar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnCargar);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);
        return panel;
    }

    private static void cargarDatosEnTabla(DefaultTableModel model) {
        model.setRowCount(0); // Limpiar la tabla antes de cargar los datos
        List<String[]> usuarios = obtenerUsuarios();
        for (String[] usuario : usuarios) {
            if (usuario[5].isEmpty()) {
                model.addRow(new Object[]{usuario[0], usuario[1], usuario[2], usuario[3], usuario[4], null});
            } else {
                model.addRow(new Object[]{usuario[0], usuario[1], usuario[2], usuario[3], usuario[4], new ImageIcon(IMAGE_FOLDER + usuario[5])});
            }
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        createCSVFile();
        crearInterfaz();
    }

}
