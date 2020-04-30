package cz.iwitrag.greencore.storage;

import org.hibernate.Session;

@FunctionalInterface
public interface HibernateTask {

    void run(Session session);

}
