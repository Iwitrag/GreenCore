package cz.iwitrag.greencore.storage;

import org.hibernate.dialect.MySQL5InnoDBDialect;

// BASED ON: https://stackoverflow.com/a/26371637/2872536
// AND: https://stackoverflow.com/a/54993738/2872536
public class ImprovedMySQL5InnoDBDialect extends MySQL5InnoDBDialect {
    @Override
    public String getDropSequenceString(String sequenceName) {
        // Adding the "if exists" clause to avoid warnings
        return "drop sequence if exists " + sequenceName;
    }

    @Override
    public boolean dropConstraints() {
        // We don't need to drop constraints before dropping tables, that just leads to error
        // messages about missing tables when we don't have a schema in the database
        return false;
    }
    @Override
    public String getTableTypeString() {
        // Custom collation (fakaheda uses swedish_ci which we don't want as default)
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci";
    }
}