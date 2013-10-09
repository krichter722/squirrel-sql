package org.squirrelsql.aliases.dbconnector;

import javafx.stage.Window;
import org.squirrelsql.AppState;
import org.squirrelsql.aliases.Alias;
import org.squirrelsql.aliases.AliasUtil;
import org.squirrelsql.drivers.DriversUtil;
import org.squirrelsql.services.sqlwrap.SQLDriver;
import org.squirrelsql.services.sqlwrap.SQLDriverClassLoader;
import org.squirrelsql.services.CancelableProgressTask;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnector
{
   private Alias _alias;
   private Window _owner;

   public DBConnector(Alias alias, Window owner)
   {
      _alias = alias;

      _owner = owner;
      if (null == _owner)
      {
         _owner = AppState.get().getPrimaryStage();
      }
   }


   public void tryConnect(DbConnectorListener dbConnectorListener)
   {
      _tryConnect(dbConnectorListener, false);
   }

   private void _tryConnect(final DbConnectorListener dbConnectorListener, boolean forceLogin)
   {

      final String user[] = new String[1];
      final String password[] = new String[1];


      if(false == _alias.isAutoLogon() || forceLogin)
      {
         LoginController loginController = new LoginController(_alias, _owner);

         if(false == loginController.isOk())
         {
            DbConnectorResult dbConnectorResult = new DbConnectorResult(_alias, null);
            dbConnectorResult.setLoginCanceled(true);
            dbConnectorListener.finished(dbConnectorResult);
            return;
         }

         user[0] = loginController.getUserName();
         password[0] = loginController.getPassword();
      }
      else
      {
         user[0] = AliasUtil.getUserNameRespectAliasConfig(_alias);
         password[0] = AliasUtil.getPasswordRespectAliasConfig(_alias);
      }


      final ConnectingController connectingController = new ConnectingController(_owner, _alias);

      CancelableProgressTask<DbConnectorResult> pt = new CancelableProgressTask<DbConnectorResult>()
      {
         @Override
         public DbConnectorResult call()
         {
            return doTryConnect(user[0], password[0]);
         }

         @Override
         public void goOn(DbConnectorResult dbConnectorResult)
         {
            onGoOn(dbConnectorResult, dbConnectorListener, connectingController);
         }

         @Override
         public void cancel()
         {
            onCanceled(dbConnectorListener, connectingController, user[0]);
         }
      };

      connectingController.startConnecting(pt);





   }

   private void onCanceled(DbConnectorListener dbConnectorListener, ConnectingController connectingController, String user)
   {
      DbConnectorResult dbConnectorResult = new DbConnectorResult(_alias, user);
      dbConnectorResult.setCanceled(true);
      onGoOn(dbConnectorResult, dbConnectorListener, connectingController);
   }

   private DbConnectorResult doTryConnect(String user, String password)
   {
      try
      {
         SQLDriver sqlDriver = DriversUtil.findDriver(_alias.getDriverId());

         SQLDriverClassLoader driverClassLoader = DriversUtil.createDriverClassLoader(sqlDriver.getJarFileNamesList());

         Driver driver = (Driver)(Class.forName(sqlDriver.getDriverClassName(), false, driverClassLoader).newInstance());

         Properties myProps = new Properties();
         if (user != null)
         {
            myProps.put("user", user);
         }
         if (password != null)
         {
            myProps.put("password", password);
         }

         Connection jdbcConn = driver.connect(_alias.getUrl(), myProps);

         // TODO SQLConnection drum und rein ins dbConnectorResult

         DbConnectorResult dbConnectorResult = new DbConnectorResult(_alias, user);
         dbConnectorResult.setConnectException(new SQLException("Nix Connection"));
         return dbConnectorResult;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private void onGoOn(DbConnectorResult dbConnectorResult, DbConnectorListener dbConnectorListener, ConnectingController connectingController)
   {
      if(dbConnectorResult.isCanceled())
      {
         connectingController.close();
         dbConnectorListener.finished(dbConnectorResult);
      }
      else if(false == dbConnectorResult.isConnected())
      {
         connectingController.displayAndDecideOnConnectFailure(dbConnectorResult, d -> onConnectFailureDecision(d, dbConnectorResult, dbConnectorListener));
      }
      else
      {
         connectingController.close();
         dbConnectorListener.finished(dbConnectorResult);
      }



   }

   private void onConnectFailureDecision(ConnectFailureDecisionListener.Decision decision, DbConnectorResult dbConnectorResult, DbConnectorListener dbConnectorListener)
   {
      if(ConnectFailureDecisionListener.Decision.RELOGIN_REQUESTED == decision)
      {
         _tryConnect(dbConnectorListener, true);
      }
      else
      {
         dbConnectorResult.setEditAliasRequested(true);
         dbConnectorListener.finished(dbConnectorResult);
      }
   }
}
