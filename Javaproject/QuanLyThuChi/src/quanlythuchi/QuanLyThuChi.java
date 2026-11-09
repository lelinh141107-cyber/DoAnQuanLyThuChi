package quanlythuchi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;

public class QuanLyThuChi extends JFrame {

    // 1. KET NOI SQL SERVER
    private static final String URL =
            "jdbc:sqlserver://localhost;"
            + "instanceName=SQLEXPRESS;"
            + "databaseName=QuanLyThuChi;"
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "integratedSecurity=true;";

    private static final int MA_NGUOI_DUNG = 1;

    private Connection conn;

    private JComboBox<Item> cboDanhMuc;
    private JComboBox<Item> cboPhuongThuc;
    private JComboBox<String> cboLoai;

    private JTextField txtSoTien;
    private JTextField txtNgay;
    private JTextField txtGhiChu;

    private JTable table;
    private DefaultTableModel model;

    private JLabel lblTongThu;
    private JLabel lblTongChi;
    private JLabel lblSoDu;

    private int selectedId = -1;

    private final DecimalFormat df =
            new DecimalFormat("#,##0.##");

    // LUU MA VA TEN DANH MUC, PHUONG THUC
    static class Item {
        int id;
        String name;

        Item(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // 2. KHOI TAO CHUONG TRINH
    public QuanLyThuChi() {

        setTitle("PHAN MEM QUAN LY THU CHI CA NHAN");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        connectDatabase();
        createGUI();

        // TAO MENU CHUYEN DOI CAC FORM
        createNavigationMenu();

        if (conn != null) {
            loadDanhMuc();
            loadPhuongThuc();
            loadTable();
            updateStatistics();
        }
    }

    // 3. KET NOI SQL SERVER
    private void connectDatabase() {

        try {
            Class.forName(
                    "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            );

            conn = DriverManager.getConnection(URL);

            System.out.println(
                    "Ket noi SQL Server thanh cong!"
            );

        } catch (Exception e) {

            conn = null;

            JOptionPane.showMessageDialog(
                    this,
                    "Khong ket noi duoc SQL Server!\n"
                    + e.getMessage(),
                    "Loi ket noi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // 4. TAO GIAO DIEN
    private void createGUI() {

        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                "QUAN LY THU CHI CA NHAN",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(new Color(20, 100, 70));

        title.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 5, 15, 5
                )
        );

        add(title, BorderLayout.NORTH);

        // FORM NHAP LIEU
        JPanel form = new JPanel(
                new GridLayout(6, 2, 10, 10)
        );

        form.setBorder(
                BorderFactory.createTitledBorder(
                        "THONG TIN GIAO DICH"
                )
        );

        cboLoai = new JComboBox<>(
                new String[]{"Thu", "Chi"}
        );

        cboDanhMuc = new JComboBox<>();
        cboPhuongThuc = new JComboBox<>();

        txtSoTien = new JTextField();

        txtNgay = new JTextField(
                java.time.LocalDate.now().toString()
        );

        txtGhiChu = new JTextField();

        form.add(new JLabel("Loai giao dich:"));
        form.add(cboLoai);

        form.add(new JLabel("Danh muc:"));
        form.add(cboDanhMuc);

        form.add(new JLabel("So tien:"));
        form.add(txtSoTien);

        form.add(new JLabel("Ngay giao dich (yyyy-MM-dd):"));
        form.add(txtNgay);

        form.add(new JLabel("Phuong thuc:"));
        form.add(cboPhuongThuc);

        form.add(new JLabel("Ghi chu:"));
        form.add(txtGhiChu);

        cboLoai.addActionListener(e -> loadDanhMuc());

        JPanel left = new JPanel(new BorderLayout());

        left.add(form, BorderLayout.CENTER);

        // CAC NUT CHUC NANG
        JPanel buttons = new JPanel(
                new GridLayout(1, 4, 8, 8)
        );

        JButton btnThem = new JButton("Them");
        JButton btnSua = new JButton("Sua");
        JButton btnXoa = new JButton("Xoa");
        JButton btnMoi = new JButton("Lam moi");

        buttons.add(btnThem);
        buttons.add(btnSua);
        buttons.add(btnXoa);
        buttons.add(btnMoi);

        left.add(buttons, BorderLayout.SOUTH);

        add(left, BorderLayout.WEST);

        btnThem.addActionListener(e -> addTransaction());
        btnSua.addActionListener(e -> updateTransaction());
        btnXoa.addActionListener(e -> deleteTransaction());
        btnMoi.addActionListener(e -> resetForm());

        // BANG GIAO DICH
        model = new DefaultTableModel(
                new String[]{
                    "Ma GD",
                    "Loai",
                    "Danh muc",
                    "So tien",
                    "Ngay",
                    "Phuong thuc",
                    "Ghi chu"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(
                    int row, int col
            ) {
                return false;
            }
        };

        table = new JTable(model);

        table.setRowHeight(25);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createTitledBorder(
                        "DANH SACH GIAO DICH"
                )
        );

        add(scroll, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectRow();
            }
        });

        // THONG KE THU CHI
        JPanel statistics = new JPanel(
                new GridLayout(1, 3, 15, 10)
        );

        lblTongThu = new JLabel("Tong thu: 0 VND");
        lblTongChi = new JLabel("Tong chi: 0 VND");
        lblSoDu = new JLabel("So du: 0 VND");

        lblTongThu.setForeground(new Color(0, 130, 60));
        lblTongChi.setForeground(Color.RED);
        lblSoDu.setForeground(Color.BLUE);

        lblTongThu.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        lblTongChi.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        lblSoDu.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        statistics.add(lblTongThu);
        statistics.add(lblTongChi);
        statistics.add(lblSoDu);

        statistics.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 10, 15, 10
                )
        );

        add(statistics, BorderLayout.SOUTH);
    }

    // 5. TAO MENU QUAN LY
    private void createNavigationMenu() {

        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("QUAN LY");

        JMenuItem itemGiaoDich =
                new JMenuItem("Quan ly giao dich");

        JMenuItem itemDanhMuc =
                new JMenuItem("Quan ly danh muc");

        JMenuItem itemPhuongThuc =
                new JMenuItem("Quan ly phuong thuc thanh toan");

        itemGiaoDich.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Ban dang o man hinh quan ly giao dich!"
            );
        });

        itemDanhMuc.addActionListener(e -> {
            new FormDanhMuc().setVisible(true);
        });

        itemPhuongThuc.addActionListener(e -> {
            new FormPhuongThuc().setVisible(true);
        });

        menu.add(itemGiaoDich);
        menu.add(itemDanhMuc);
        menu.add(itemPhuongThuc);

        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    // 6. LOAD DANH MUC
    private void loadDanhMuc() {

        if (conn == null || cboDanhMuc == null) {
            return;
        }

        cboDanhMuc.removeAllItems();

        String sql =
                "SELECT MaDanhMuc, TenDanhMuc "
                + "FROM DanhMuc "
                + "WHERE Loai = ?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    cboLoai.getSelectedItem().toString()
            );

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    cboDanhMuc.addItem(
                            new Item(
                                    rs.getInt("MaDanhMuc"),
                                    rs.getString("TenDanhMuc")
                            )
                    );
                }
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    // 7. LOAD PHUONG THUC
    private void loadPhuongThuc() {

        if (conn == null) {
            return;
        }

        cboPhuongThuc.removeAllItems();

        String sql =
                "SELECT MaPhuongThuc, TenPhuongThuc "
                + "FROM PhuongThuc";

        try (PreparedStatement ps =
                conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                cboPhuongThuc.addItem(
                        new Item(
                                rs.getInt("MaPhuongThuc"),
                                rs.getString("TenPhuongThuc")
                        )
                );
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    // 8. HIEN THI DANH SACH GIAO DICH
    private void loadTable() {

        if (conn == null || model == null) {
            return;
        }

        model.setRowCount(0);

        String sql =
                "SELECT G.MaGiaoDich, D.Loai, "
                + "D.TenDanhMuc, G.SoTien, "
                + "G.NgayGiaoDich, P.TenPhuongThuc, "
                + "G.NoiDung "
                + "FROM GiaoDich G "
                + "JOIN DanhMuc D "
                + "ON G.MaDanhMuc = D.MaDanhMuc "
                + "JOIN PhuongThuc P "
                + "ON G.MaPhuongThuc = P.MaPhuongThuc "
                + "WHERE G.MaNguoiDung = ? "
                + "ORDER BY G.NgayGiaoDich DESC";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, MA_NGUOI_DUNG);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    BigDecimal soTien =
                            rs.getBigDecimal("SoTien");

                    model.addRow(new Object[]{
                        rs.getInt("MaGiaoDich"),
                        rs.getString("Loai"),
                        rs.getString("TenDanhMuc"),
                        df.format(soTien),
                        rs.getDate("NgayGiaoDich"),
                        rs.getString("TenPhuongThuc"),
                        rs.getString("NoiDung")
                    });
                }
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    // 9. THEM GIAO DICH
    private void addTransaction() {

        if (conn == null) {
            showError(new Exception("Chua ket noi SQL Server!"));
            return;
        }

        try {

            Item danhMuc =
                    (Item) cboDanhMuc.getSelectedItem();

            Item phuongThuc =
                    (Item) cboPhuongThuc.getSelectedItem();

            if (danhMuc == null || phuongThuc == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Vui long tao danh muc va phuong thuc truoc!"
                );

                return;
            }

            BigDecimal soTien = new BigDecimal(
                    txtSoTien.getText().trim()
            );

            if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "So tien phai lon hon 0!"
                );
            }

            Date ngay = Date.valueOf(
                    txtNgay.getText().trim()
            );

            String sql =
                    "INSERT INTO GiaoDich "
                    + "(MaNguoiDung, MaDanhMuc, "
                    + "MaPhuongThuc, SoTien, "
                    + "NgayGiaoDich, NoiDung) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps =
                    conn.prepareStatement(sql)) {

                ps.setInt(1, MA_NGUOI_DUNG);
                ps.setInt(2, danhMuc.id);
                ps.setInt(3, phuongThuc.id);
                ps.setBigDecimal(4, soTien);
                ps.setDate(5, ngay);
                ps.setString(6, txtGhiChu.getText());

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Them giao dich thanh cong!"
            );

            resetForm();
            refreshData();

        } catch (Exception e) {
            showError(e);
        }
    }

    // 10. CHON DONG DE SUA
    private void selectRow() {

        int row = table.getSelectedRow();

        if (row < 0) {
            return;
        }

        selectedId = Integer.parseInt(
                model.getValueAt(row, 0).toString()
        );

        String loai =
                model.getValueAt(row, 1).toString();

        cboLoai.setSelectedItem(loai);

        loadDanhMuc();

        String tenDanhMuc =
                model.getValueAt(row, 2).toString();

        for (int i = 0;
                i < cboDanhMuc.getItemCount(); i++) {

            if (cboDanhMuc.getItemAt(i).name.equals(
                    tenDanhMuc
            )) {
                cboDanhMuc.setSelectedIndex(i);
                break;
            }
        }

        txtSoTien.setText(
                model.getValueAt(row, 3)
                        .toString()
                        .replace(",", "")
        );

        txtNgay.setText(
                model.getValueAt(row, 4).toString()
        );

        String tenPhuongThuc =
                model.getValueAt(row, 5).toString();

        for (int i = 0;
                i < cboPhuongThuc.getItemCount(); i++) {

            if (cboPhuongThuc.getItemAt(i).name.equals(
                    tenPhuongThuc
            )) {
                cboPhuongThuc.setSelectedIndex(i);
                break;
            }
        }

        Object ghiChu = model.getValueAt(row, 6);

        txtGhiChu.setText(
                ghiChu == null ? "" : ghiChu.toString()
        );
    }

    // 11. SUA GIAO DICH
    private void updateTransaction() {

        if (conn == null) {
            showError(new Exception("Chua ket noi SQL Server!"));
            return;
        }

        if (selectedId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui long chon giao dich can sua!"
            );

            return;
        }

        try {

            Item danhMuc =
                    (Item) cboDanhMuc.getSelectedItem();

            Item phuongThuc =
                    (Item) cboPhuongThuc.getSelectedItem();

            if (danhMuc == null || phuongThuc == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Danh muc hoac phuong thuc khong hop le!"
                );

                return;
            }

            BigDecimal soTien = new BigDecimal(
                    txtSoTien.getText().trim()
            );

            if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "So tien phai lon hon 0!"
                );
            }

            Date ngay = Date.valueOf(
                    txtNgay.getText().trim()
            );

            String sql =
                    "UPDATE GiaoDich SET "
                    + "MaDanhMuc=?, "
                    + "MaPhuongThuc=?, "
                    + "SoTien=?, "
                    + "NgayGiaoDich=?, "
                    + "NoiDung=? "
                    + "WHERE MaGiaoDich=? "
                    + "AND MaNguoiDung=?";

            try (PreparedStatement ps =
                    conn.prepareStatement(sql)) {

                ps.setInt(1, danhMuc.id);
                ps.setInt(2, phuongThuc.id);
                ps.setBigDecimal(3, soTien);
                ps.setDate(4, ngay);
                ps.setString(5, txtGhiChu.getText());
                ps.setInt(6, selectedId);
                ps.setInt(7, MA_NGUOI_DUNG);

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Cap nhat giao dich thanh cong!"
            );

            resetForm();
            refreshData();

        } catch (Exception e) {
            showError(e);
        }
    }

    // 12. XOA GIAO DICH
    private void deleteTransaction() {

        if (conn == null) {
            showError(new Exception("Chua ket noi SQL Server!"));
            return;
        }

        if (selectedId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Vui long chon giao dich can xoa!"
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Ban co chac chan muon xoa giao dich nay?",
                "Xac nhan",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "DELETE FROM GiaoDich "
                + "WHERE MaGiaoDich=? "
                + "AND MaNguoiDung=?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, selectedId);
            ps.setInt(2, MA_NGUOI_DUNG);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    "Xoa giao dich thanh cong!"
            );

            resetForm();
            refreshData();

        } catch (SQLException e) {
            showError(e);
        }
    }

    // 13. THONG KE THU CHI
    private void updateStatistics() {

        if (conn == null) {
            return;
        }

        String sql =
                "SELECT "
                + "SUM(CASE WHEN D.Loai = N'Thu' "
                + "THEN G.SoTien ELSE 0 END) AS TongThu, "
                + "SUM(CASE WHEN D.Loai = N'Chi' "
                + "THEN G.SoTien ELSE 0 END) AS TongChi "
                + "FROM GiaoDich G "
                + "JOIN DanhMuc D "
                + "ON G.MaDanhMuc = D.MaDanhMuc "
                + "WHERE G.MaNguoiDung=?";

        try (PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setInt(1, MA_NGUOI_DUNG);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    BigDecimal thu =
                            rs.getBigDecimal("TongThu");

                    BigDecimal chi =
                            rs.getBigDecimal("TongChi");

                    if (thu == null) {
                        thu = BigDecimal.ZERO;
                    }

                    if (chi == null) {
                        chi = BigDecimal.ZERO;
                    }

                    BigDecimal soDu = thu.subtract(chi);

                    lblTongThu.setText(
                            "Tong thu: "
                            + df.format(thu)
                            + " VND"
                    );

                    lblTongChi.setText(
                            "Tong chi: "
                            + df.format(chi)
                            + " VND"
                    );

                    lblSoDu.setText(
                            "So du: "
                            + df.format(soDu)
                            + " VND"
                    );
                }
            }

        } catch (SQLException e) {
            showError(e);
        }
    }

    // 14. LAM MOI DU LIEU
    private void refreshData() {

        loadTable();
        updateStatistics();
    }

    // 15. LAM MOI FORM
    private void resetForm() {

        selectedId = -1;

        txtSoTien.setText("");
        txtGhiChu.setText("");

        txtNgay.setText(
                java.time.LocalDate.now().toString()
        );

        table.clearSelection();
    }

    // 16. HIEN THI THONG BAO LOI
    private void showError(Exception e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Thong bao loi",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // 17. HAM MAIN
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new QuanLyThuChi().setVisible(true);
        });
    }
}