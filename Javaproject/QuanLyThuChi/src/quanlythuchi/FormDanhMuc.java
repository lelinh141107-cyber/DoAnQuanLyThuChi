
package quanlythuchi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormDanhMuc extends JFrame {

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
    private JComboBox<String> cboLoai;
    private int selectedId = -1;

    public FormDanhMuc() {
        setTitle("QUAN LY DANH MUC");
        setSize(750, 500);
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
            "QUAN LY DANH MUC",
            SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setForeground(new Color(20, 100, 70));

        JPanel form = new JPanel(
            new GridLayout(2, 2, 10, 10)
        );
        form.setBorder(
            BorderFactory.createTitledBorder("THONG TIN DANH MUC")
        );

        txtTen = new JTextField();
        cboLoai = new JComboBox<>(
            new String[]{"Thu", "Chi"}
        );

        form.add(new JLabel("Ten danh muc:"));
        form.add(txtTen);
        form.add(new JLabel("Loai danh muc:"));
        form.add(cboLoai);

        JPanel buttons = new JPanel();

        JButton btnThem = new JButton("Them");
        JButton btnSua = new JButton("Sua");
        JButton btnXoa = new JButton("Xoa");
        JButton btnMoi = new JButton("Lam moi");

        buttons.add(btnThem);
        buttons.add(btnSua);
        buttons.add(btnXoa);
        buttons.add(btnMoi);

        JPanel top = new JPanel(new BorderLayout());
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        top.add(title, BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(
            new String[]{"Ma danh muc", "Ten danh muc", "Loai"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel search = new JPanel();

        txtTim = new JTextField(20);
        JButton btnTim = new JButton("Tim kiem");

        search.add(new JLabel("Ten danh muc:"));
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
            "SELECT MaDanhMuc, TenDanhMuc, Loai FROM DanhMuc";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("MaDanhMuc"),
                    rs.getString("TenDanhMuc"),
                    rs.getString("Loai")
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
                this, "Vui long nhap ten danh muc!"
            );
            return;
        }

        String sql =
            "INSERT INTO DanhMuc(TenDanhMuc, Loai) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtTen.getText().trim());
            ps.setString(2, cboLoai.getSelectedItem().toString());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Them danh muc thanh cong!"
            );

            resetForm();
            loadData();

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void updateData() {
        if (conn == null) return;

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(
                this, "Vui long chon danh muc can sua!"
            );
            return;
        }

        if (txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Ten danh muc khong duoc de trong!"
            );
            return;
        }

        String sql =
            "UPDATE DanhMuc SET TenDanhMuc=?, Loai=? "
            + "WHERE MaDanhMuc=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtTen.getText().trim());
            ps.setString(2, cboLoai.getSelectedItem().toString());
            ps.setInt(3, selectedId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Cap nhat danh muc thanh cong!"
            );

            resetForm();
            loadData();

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void deleteData() {
        if (conn == null) return;

        if (selectedId == -1) {
            JOptionPane.showMessageDialog(
                this, "Vui long chon danh muc can xoa!"
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Ban co chac chan muon xoa danh muc nay?",
            "Xac nhan",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql =
            "DELETE FROM DanhMuc WHERE MaDanhMuc=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Xoa danh muc thanh cong!"
            );

            resetForm();
            loadData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "Khong the xoa danh muc. Co the danh muc dang "
                + "duoc su dung trong giao dich.\n" + e.getMessage()
            );
        }
    }

    private void searchData() {
        if (conn == null) return;

        model.setRowCount(0);

        String sql =
            "SELECT MaDanhMuc, TenDanhMuc, Loai "
            + "FROM DanhMuc WHERE TenDanhMuc LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + txtTim.getText().trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("MaDanhMuc"),
                        rs.getString("TenDanhMuc"),
                        rs.getString("Loai")
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
        cboLoai.setSelectedItem(model.getValueAt(row, 2).toString());
    }

    private void resetForm() {
        selectedId = -1;
        txtTen.setText("");
        txtTim.setText("");
        cboLoai.setSelectedIndex(0);
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
