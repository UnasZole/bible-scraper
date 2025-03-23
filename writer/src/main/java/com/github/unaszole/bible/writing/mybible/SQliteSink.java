package com.github.unaszole.bible.writing.mybible;

import com.github.unaszole.bible.writing.datamodel.DocumentMetadata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

public class SQliteSink implements VerseSink {

    private final Connection sqliteConnection;
    private final PreparedStatement insertStatement;

    private static void validateMybibleFile(Path outSqlite) {
        if(!outSqlite.getFileName().toString().endsWith(".bbl.mybible")) {
            throw new IllegalArgumentException(outSqlite + " file name must end with .bbl.mybible");
        }
    }

    public SQliteSink(Path outSqlite, DocumentMetadata meta) throws SQLException, IOException {
        validateMybibleFile(outSqlite);

        // Output starts from a clean file.
        Files.deleteIfExists(outSqlite);

        this.sqliteConnection = DriverManager.getConnection("jdbc:sqlite:" + outSqlite);

        sqliteConnection.createStatement()
                        .execute("CREATE TABLE Details (Title NVARCHAR(255), Description TEXT, Abbreviation NVARCHAR(50), Comments TEXT, Version TEXT, VersionDate DATETIME, PublishDate DATETIME, Publisher TEXT, Author TEXT, Creator TEXT, Source TEXT, EditorialComments TEXT, Language NVARCHAR(3), RightToLeft BOOL, OT BOOL, NT BOOL, Strong BOOL, VerseRules TEXT)");
        PreparedStatement insertDetailsStatement = sqliteConnection.prepareStatement("INSERT INTO Details (Title, Description, Abbreviation, Comments, Version, VersionDate, Language, RightToLeft, OT, NT, Strong) VALUES(?,?,?,?,?,?,?,?,?,?,?)");
        insertDetailsStatement.setString(1, meta.title);
        insertDetailsStatement.setString(2, "");
        insertDetailsStatement.setString(3, meta.systemName);
        insertDetailsStatement.setString(4, "");
        insertDetailsStatement.setString(5, "0.1");
        insertDetailsStatement.setDate(6, new java.sql.Date(Instant.now().toEpochMilli()));
        insertDetailsStatement.setString(7, meta.locale.getISO3Language());
        insertDetailsStatement.setBoolean(8, false);
        insertDetailsStatement.setBoolean(9, true);
        insertDetailsStatement.setBoolean(10, true);
        insertDetailsStatement.setBoolean(11, false);
        insertDetailsStatement.executeUpdate();

        sqliteConnection.createStatement()
                .execute("CREATE TABLE Bible (Book INT, Chapter INT, Verse INT, Scripture TEXT)");
        this.insertStatement = sqliteConnection.prepareStatement("INSERT INTO Bible(Book, Chapter, Verse, Scripture) VALUES(?, ?, ?, ?)");
    }

    @Override
    public void append(int bookNb, int chapterNb, int verseNb, String verseText) {
        try {
            insertStatement.setInt(1, bookNb);
            insertStatement.setInt(2, chapterNb);
            insertStatement.setInt(3, verseNb);
            insertStatement.setString(4, verseText);
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        sqliteConnection.close();
    }
}
