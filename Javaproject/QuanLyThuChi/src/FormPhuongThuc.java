
package quanlythuchi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormPhuongThuc extends JFrame {

    private static final String URL =
            "jdbc:sqlserver://localhost;"
            + "instanceName=SQLEXPRESS;"
            + "databaseName=QuanLyThuChi;"
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "integratedSecurity=true;";

    private Connection conn;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtTen, txtTim;
    private int selectedId = -1;

    public FormPhuongThuc() {
        setTitle("QUAN LY PHUONG THUC THANH TOAN");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        connectDatabase();
        createGUI();
        loadData();
    }

    private void connectDatabase() {
        try {
            Class.forName(
                "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            );
            conn = DriverManager.getConnection(URL);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this, "Loi ket noi SQL: " + e.getMessage()
            );
        }
    }

    private void createGUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
            "QUAN LY PHUONG THUC THANH TOAN",
            SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(20, 100, 70));

        txtTen = new JTextField();

        JPanel form = new JPanel(
            new GridLayout(1, 2, 10, 10)
        );

        form.setBorder(
            BorderFactory.createTitledBorder("THONG TIN PHUONG THUC")
        );

        form.add(new JLabel("Ten phuong thuc:"));
        form.add(txtTen);

        JButton btnThem = new JButton("Them");
        JButton btnSua = new JButton("Sua");
        JButton btnXoa = new JButton("Xoa");
        JButton btnMoi = new JButton("Lam moi");

        JPanel buttons = new JPanel();
        buttons.add(btnThem);
        buttons.add(btnSua);
        buttons.add(btnXoa);
        buttons.add(btnMoi);

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(title, BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"Ma phuong thuc", "Ten phuong thuc"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);

        txtTim = new JTextField(20);
        JButton btnTim = new JButton("Tim kiem");

        JPanel search = new JPanel();
        search.add(new JLabel("Ten phuong thuc:"));
        search.add(txtTim);
        search.add(btnTim);

        add(search, BorderLayout.SOUTH);

        btnThem.addActionListener(e -> addData());
        btnSua.addActionListener(e -> updateData());
        btnXoa.addActionListener(e -> deleteData());
        btnMoi.addActionListener(e -> resetForm());
        btnTim.addActionListener(e -> searchData());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });
    }

    private void loadData() {
        if (conn == null) return;

        model.setRowCount(0);

        String sql =
            "SELECT MaPhuongThuc, TenPhuongThuc FROM PhuongThuc";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("MaPhuongThuc"),
                    rs.getString("TenPhuongThuc")
                });
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void addData() {
        if (conn == null) return;

        if (txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Vui long nhap ten phuong thuc!"
            );
            return;
        }

        String sql =
            "INSERT INTO PhuongThuc(TenPhuongThuc) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtTen.getText().trim());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Them phuong thuc thanh cong!"
            );

            resetForm();

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void updateData() {
        if (conn == null) return;

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(
                this, "Vui long chon phuong thuc can sua!"
            );
            return;
        }

        if (txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Ten phuong thuc khong duoc de trong!"
            );
            return;
        }

        String sql =
            "UPDATE PhuongThuc SET TenPhuongThuc=? "
            + "WHERE MaPhuongThuc=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtTen.getText().trim());
            ps.setInt(2, selectedId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Cap nhat phuong thuc thanh cong!"
            );

            resetForm();

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void deleteData() {
        if (conn == null) return;

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(
                this, "Vui long chon phuong thuc can xoa!"
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Ban co chac chan muon xoa phuong thuc nay?",
            "Xac nhan",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql =
            "DELETE FROM PhuongThuc WHERE MaPhuongThuc=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Xoa phuong thuc thanh cong!"
            );

            resetForm();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Khong the xoa phuong thuc. Co the phuong thuc "
                + "dang duoc su dung trong giao dich.\n"
                + e.getMessage()
            );
        }
    }

    private void searchData() {
        if (conn == null) return;

        model.setRowCount(0);

        String sql =
            "SELECT MaPhuongThuc, TenPhuongThuc "
            + "FROM PhuongThuc WHERE TenPhuongThuc LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + txtTim.getText().trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("MaPhuongThuc"),
                        rs.getString("TenPhuongThuc")
                    });
                }
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void selectRow() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        selectedId = Integer.parseInt(
            model.getValueAt(row, 0).toString()
        );

        txtTen.setText(model.getValueAt(row, 1).toString());
    }

    private void resetForm() {
        selectedId = -1;
        txtTen.setText("");
        txtTim.setText("");
        table.clearSelection();
        loadData();
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(
            this, e.getMessage(),
            "Thong bao loi", JOptionPane.ERROR_MESSAGE
        );
    }
}
