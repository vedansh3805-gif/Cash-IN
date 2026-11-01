package com.cashflow.repository;

import com.cashflow.model.DatasetVersion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Repository
public class DatasetVersionRepository {
    private final JdbcTemplate jdbcTemplate;

    public DatasetVersionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDateTime.parse(raw.replace(" ", "T"));
        } catch (Exception e) {
            try {
                return LocalDate.parse(raw).atStartOfDay();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private final RowMapper<DatasetVersion> rowMapper = (rs, rowNum) -> {
        DatasetVersion v = new DatasetVersion();
        v.setVersionId(rs.getString("version_id"));
        v.setVersionNumber(rs.getInt("version_number"));
        v.setFileName(rs.getString("file_name"));
        v.setUploadDate(parseDateTime(rs.getString("upload_date")));
        v.setRecordCount(rs.getInt("record_count"));
        v.setPreviousVersionId(rs.getString("previous_version_id"));
        return v;
    };

    public void save(DatasetVersion v) {
        String sql = "INSERT INTO dataset_version (version_id, version_number, file_name, upload_date, record_count, previous_version_id) VALUES (?, ?, ?, ?, ?, ?)";
        String formattedDate = v.getUploadDate() != null ? v.getUploadDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
        jdbcTemplate.update(sql, v.getVersionId(), v.getVersionNumber(), v.getFileName(), formattedDate, v.getRecordCount(), v.getPreviousVersionId());
    }

    public List<DatasetVersion> findAll() {
        return jdbcTemplate.query("SELECT * FROM dataset_version ORDER BY version_number DESC", rowMapper);
    }

    public Optional<DatasetVersion> findLatest() {
        List<DatasetVersion> list = jdbcTemplate.query("SELECT * FROM dataset_version ORDER BY version_number DESC LIMIT 1", rowMapper);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
    
    public Optional<DatasetVersion> findById(String id) {
        List<DatasetVersion> list = jdbcTemplate.query("SELECT * FROM dataset_version WHERE version_id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
