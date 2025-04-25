package wsulu.document.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wsulu.document.model.UserEntity;

@Repository
public interface UserRepo extends JpaRepository<UserEntity, Long> {

    @Query(
            value =
                    "SELECT * FROM users AS u "
                            + "WHERE u.username=:username order by u.id desc limit 1",
            nativeQuery = true)
    UserEntity findOneByUserName(@Param("username") String username);
}
