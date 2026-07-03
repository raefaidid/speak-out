package com.speakout.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.speakout.bean.CategoryBean;
import com.speakout.util.DBConnection;

public class CategoryDAO {

    public List<CategoryBean> listAll() throws SQLException {
        List<CategoryBean> out = new ArrayList<>();
        try (Connection c = DBConnection.createConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT category_id, name, description FROM category ORDER BY name")) {
            while (rs.next()) {
                CategoryBean cat = new CategoryBean();
                cat.setCategoryId(rs.getString("category_id"));
                cat.setName(rs.getString("name"));
                cat.setDescription(rs.getString("description"));
                out.add(cat);
            }
        }
        return out;
    }
}
