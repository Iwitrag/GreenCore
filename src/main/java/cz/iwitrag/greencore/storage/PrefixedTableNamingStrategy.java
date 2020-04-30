package cz.iwitrag.greencore.storage;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

// BASED ON: https://stackoverflow.com/a/52781557/2872536
public class PrefixedTableNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    public static final String TABLE_NAME_PREFIX = "gcore_";

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        Identifier newIdentifier = new Identifier(TABLE_NAME_PREFIX + name.getText(), name.isQuoted());
        return super.toPhysicalTableName(newIdentifier, context);
    }
}