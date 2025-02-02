package com.test.demo.repositories.databasePermissionRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.test.demo.entities.databasePermission.DatabasePermission;
import com.test.demo.entities.databasePermission.DatabasePermissionDTO;


@Repository
public interface DatabasePermissionRepository extends JpaRepository<DatabasePermission, String>{

    @Query("SELECT new com.test.demo.entities.databasePermission.DatabasePermissionDTO(dp.dbHost, dp.dbName, dp.dbPort, dp.dbUser) FROM DatabasePermission dp WHERE dp.user.id = :userId")
    List<DatabasePermissionDTO> findDatabasePermissionsByUserId(@Param("userId") String userId);
    
    @Query("SELECT new com.test.demo.entities.databasePermission.DatabasePermissionDTO(dp.dbName, dp.dbHost, dp.dbPort, dp.dbUser) FROM DatabasePermission dp WHERE dp.user.id = :userId AND dp.dbHost = :host AND dp.dbName = :name")
    Optional<DatabasePermissionDTO> findUniqueDatabasePermission(@Param("userId") String userId,@Param("name")String name,@Param("host")String host);

    @Modifying
    @Query("DELETE FROM DatabasePermission dp WHERE dp.user.id = :userId AND dp.dbHost = :host AND dp.dbName = :name")
    int deleteDatabasePermission(@Param("userId") String userId,@Param("name")String name,@Param("host")String host);

}
