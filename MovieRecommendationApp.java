package project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MovieRecommendationApp extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private List<Movie> movieList = new ArrayList<>();
    private DefaultListModel<String> wishlistModel = new DefaultListModel<>();
    private JPanel bottomButtonPanel;

    public MovieRecommendationApp() {
        initializeMovies();
        HomePanel homePanel = new HomePanel();
        SearchPanel searchPanel = new SearchPanel();
        MyPagePanel myPagePanel = new MyPagePanel();

        mainPanel.add(homePanel, "home");
        mainPanel.add(searchPanel, "search");
        mainPanel.add(myPagePanel, "myPage");

        setTitle("영화 추천 앱");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        bottomButtonPanel = createBottomButtonPanel();
        add(bottomButtonPanel, BorderLayout.SOUTH);

        cardLayout.show(mainPanel, "home");
        setVisible(true);
    }

    private JPanel createBottomButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        JButton homeButton = new JButton("홈");
        JButton searchButton = new JButton("검색");
        JButton myPageButton = new JButton("My 페이지");

        homeButton.addActionListener(e -> cardLayout.show(mainPanel, "home"));
        searchButton.addActionListener(e -> cardLayout.show(mainPanel, "search"));
        myPageButton.addActionListener(e -> cardLayout.show(mainPanel, "myPage"));

        buttonPanel.add(homeButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(myPageButton);

        return buttonPanel;
    }

    private void initializeMovies() {
        try {
            movieList = loadMoviesFromFile("movies.txt");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "영화 정보를 불러오는 데 실패했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Movie> loadMoviesFromFile(String fileName) throws IOException {
        List<Movie> movies = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String title = parts[0].trim();
                    String genre = parts[1].trim();
                    int year = Integer.parseInt(parts[2].trim());
                    String director = parts[3].trim();
                    String actors = parts[4].trim();
                    movies.add(new Movie(title, genre, year, director, actors));
                }
            }
        }
        return movies;
    }

    private class HomePanel extends JPanel {
        private JButton wishlistButton;

        public HomePanel() {
            setLayout(new BorderLayout());
            JLabel recommendationLabel = new JLabel("오늘의 추천 영화: 기생충", JLabel.CENTER);
            recommendationLabel.setFont(new Font("Serif", Font.BOLD, 20));
            add(recommendationLabel, BorderLayout.CENTER);

            wishlistButton = new JButton("찜하기");
            wishlistButton.addActionListener(e -> {
                if (wishlistButton.getText().equals("찜하기")) {
                    wishlistModel.addElement("기생충 (드라마, 2019)");
                    JOptionPane.showMessageDialog(null, "기생충이 찜한 목록에 추가되었습니다.");
                    wishlistButton.setText("찜 취소");
                } else {
                    wishlistModel.removeElement("기생충 (드라마, 2019)");
                    JOptionPane.showMessageDialog(null, "기생충이 찜한 목록에서 제거되었습니다.");
                    wishlistButton.setText("찜하기");
                }
            });
            add(wishlistButton, BorderLayout.SOUTH);
        }

        public void updateWishlistButton() {
            if (wishlistModel.contains("기생충 (드라마, 2019)")) {
                wishlistButton.setText("찜 취소");
            } else {
                wishlistButton.setText("찜하기");
            }
        }
    }

    private class SearchPanel extends JPanel {
        private JTextField searchField;
        private JComboBox<String> genreComboBox;
        private JTextField yearField;
        private JList<String> resultList;

        public SearchPanel() {
            setLayout(new BorderLayout());
            JPanel searchPanel = new JPanel(new FlowLayout());
            searchField = new JTextField(15);
            String[] genres = {"", "드라마", "스릴러", "액션", "코미디", "SF", "모험"};
            genreComboBox = new JComboBox<>(genres);
            yearField = new JTextField(5);
            JButton searchButton = new JButton("검색");

            searchPanel.add(new JLabel("제목:"));
            searchPanel.add(searchField);
            searchPanel.add(new JLabel("장르:"));
            searchPanel.add(genreComboBox);
            searchPanel.add(new JLabel("년도:"));
            searchPanel.add(yearField);
            searchPanel.add(searchButton);

            resultList = new JList<>();
            JScrollPane scrollPane = new JScrollPane(resultList);

            add(searchPanel, BorderLayout.NORTH);
            add(scrollPane, BorderLayout.CENTER);

            resultList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        String selectedMovie = resultList.getSelectedValue();
                        if (selectedMovie != null) {
                            showMovieDetails(selectedMovie);
                        }
                    }
                }
            });

            searchButton.addActionListener(e -> {
                String titleText = searchField.getText().toLowerCase();
                String genreText = (String) genreComboBox.getSelectedItem();
                String yearText = yearField.getText();
                DefaultListModel<String> searchResults = new DefaultListModel<>();

                for (Movie movie : movieList) {
                    boolean matchesTitle = titleText.isEmpty() || movie.getTitle().toLowerCase().contains(titleText);
                    boolean matchesGenre = genreText.isEmpty() || movie.getGenre().equalsIgnoreCase(genreText);
                    boolean matchesYear = yearText.isEmpty() || String.valueOf(movie.getYear()).equals(yearText);
                    boolean matchesDirector = movie.getDirector().toLowerCase().contains(titleText);
                    boolean matchesActors = movie.getActors().toLowerCase().contains(titleText);

                    if (matchesTitle && matchesGenre && matchesYear) {
                        searchResults.addElement(movie.getTitle() + " (" + movie.getGenre() + ", " + movie.getYear() + ")");
                    }
                }
                resultList.setModel(searchResults);
            });
        }

        private void showMovieDetails(String selectedMovie) {
            String[] parts = selectedMovie.split(" \\(");
            String title = parts[0];
            String[] genreYear = parts[1].replace(")", "").split(", ");
            String genre = genreYear[0];
            int year = Integer.parseInt(genreYear[1]);

            Movie selectedMovieObj = movieList.stream()
                    .filter(m -> m.getTitle().equals(title) && m.getGenre().equals(genre) && m.getYear() == year)
                    .findFirst()
                    .orElse(null);

            if (selectedMovieObj == null) return;

            JDialog detailDialog = new JDialog(MovieRecommendationApp.this, "영화 상세 정보", true);
            detailDialog.setLayout(new BorderLayout());
            JPanel detailPanel = new JPanel(new GridLayout(5, 2));

            detailPanel.add(new JLabel("제목:"));
            detailPanel.add(new JLabel(title));
            detailPanel.add(new JLabel("장르:"));
            detailPanel.add(new JLabel(genre));
            detailPanel.add(new JLabel("년도:"));
            detailPanel.add(new JLabel(String.valueOf(year)));
            detailPanel.add(new JLabel("감독:"));
            detailPanel.add(new JLabel(selectedMovieObj.getDirector()));
            detailPanel.add(new JLabel("주연:"));
            detailPanel.add(new JLabel(selectedMovieObj.getActors()));

            JButton wishlistButton = new JButton(wishlistModel.contains(selectedMovie) ? "찜 취소" : "찜하기");

            wishlistButton.addActionListener(e -> {
                if (wishlistModel.contains(selectedMovie)) {
                    wishlistModel.removeElement(selectedMovie);
                    JOptionPane.showMessageDialog(null, selectedMovie + "이(가) 찜한 목록에서 제거되었습니다.");
                    wishlistButton.setText("찜하기");
                } else {
                    wishlistModel.addElement(selectedMovie);
                    JOptionPane.showMessageDialog(null, selectedMovie + "이(가) 찜한 목록에 추가되었습니다.");
                    wishlistButton.setText("찜 취소");
                }
            });

            detailDialog.add(detailPanel, BorderLayout.CENTER);
            detailDialog.add(wishlistButton, BorderLayout.SOUTH);
            detailDialog.setSize(400, 300);
            detailDialog.setLocationRelativeTo(MovieRecommendationApp.this);
            detailDialog.setVisible(true);
        }
    }

    private class MyPagePanel extends JPanel {
        private JList<String> wishlistView;

        public MyPagePanel() {
            setLayout(new BorderLayout());
            JLabel titleLabel = new JLabel("찜한 영화 목록", JLabel.CENTER);
            titleLabel.setFont(new Font("Serif", Font.BOLD, 18));
            add(titleLabel, BorderLayout.NORTH);

            wishlistView = new JList<>(wishlistModel);
            JScrollPane scrollPane = new JScrollPane(wishlistView);
            add(scrollPane, BorderLayout.CENTER);

            JButton removeButton = new JButton("선택한 영화 찜 취소");
            removeButton.addActionListener(e -> {
                int selectedIndex = wishlistView.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedMovie = wishlistModel.getElementAt(selectedIndex);
                    wishlistModel.remove(selectedIndex);
                    JOptionPane.showMessageDialog(null, selectedMovie + "이(가) 찜한 목록에서 제거되었습니다.");
                    HomePanel homePanel = (HomePanel) mainPanel.getComponent(0);
                    homePanel.updateWishlistButton();
                } else {
                    JOptionPane.showMessageDialog(null, "제거할 영화를 선택해주세요.");
                }
            });
            add(removeButton, BorderLayout.SOUTH);
        }
    }

    private static class Movie {
        private String title;
        private String genre;
        private int year;
        private String director;
        private String actors;

        public Movie(String title, String genre, int year, String director, String actors) {
            this.title = title;
            this.genre = genre;
            this.year = year;
            this.director = director;
            this.actors = actors;
        }

        public String getTitle() { return title; }
        public String getGenre() { return genre; }
        public int getYear() { return year; }
        public String getDirector() { return director; }
        public String getActors() { return actors; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MovieRecommendationApp::new);
    }
}