package ratpack.example.books

import com.google.inject.Inject
import groovy.sql.Sql
import ratpack.exec.Blocking
import ratpack.exec.Promise
import ratpack.health.HealthCheck // Deprecated in v2.0 use: ratpack.core.health
import ratpack.registry.Registry
//import ratpack.core.health.HealthCheck
//import ratpack.exec.registry.Registry

class DatabaseHealthCheck implements HealthCheck {

    Sql sql

    @Inject
    DatabaseHealthCheck(Sql sql) {
        this.sql = sql
    }

    String getName() {
        return "database-health-check"
    }

    @Override
    Promise<HealthCheck.Result> check(Registry registry) throws Exception {
        Blocking.get {
            sql.rows("select count(*) from books")
            HealthCheck.Result.healthy()
        }
    }
}
