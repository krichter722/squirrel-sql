package org.squirrelsql.session.action;

import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.squirrelsql.Props;
import org.squirrelsql.services.I18n;

public enum StdActionCfg
{
   RUN_SQL("run.png", "sql.run", "runsql", ActionScope.SQL_EDITOR, new KeyCodeCombination(KeyCode.ENTER, KeyCodeCombination.CONTROL_DOWN)),
   NEW_SQL_TAB("newsqltab.png", "sql.newsqltab", null, ActionScope.UNSCOPED, new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN)),
   SQL_CODE_COMPLETION(null, "complete.code", "completecode", ActionScope.SQL_EDITOR, new KeyCodeCombination(KeyCode.SPACE, KeyCodeCombination.CONTROL_DOWN)),
   EXEC_BOOKMARK("bookmark-exec.png", "bookmark.execute", "bookmarkexecute", ActionScope.SQL_EDITOR, new KeyCodeCombination(KeyCode.J, KeyCodeCombination.CONTROL_DOWN)),
   EDIT_BOOKMARK("bookmark-edit.png", "bookmark.edit", "bookmarkedit", ActionScope.UNSCOPED, null),
   ESCAPE_DATE(null, "escape.date", "date", ActionScope.SQL_EDITOR, null),
   SQL_TO_TABLE(null, "sql.to.table", "sql2table", ActionScope.SQL_EDITOR, null),
   SHOW_TOOLS_POPUP(null, "show.tools.popup", null, ActionScope.SQL_EDITOR, new KeyCodeCombination(KeyCode.T, KeyCodeCombination.CONTROL_DOWN)),
   RELOAD_DB_META_DATA("reload.png", "reload.meta.data", "reload", ActionScope.UNSCOPED, new KeyCodeCombination(KeyCode.F5)),
   DUPLICATE_LINE_OR_SELECTION(null, "duplicate.line.or.selection", "duplicate", ActionScope.SQL_EDITOR, new KeyCodeCombination(KeyCode.D, KeyCodeCombination.CONTROL_DOWN));



   public static StdActionCfg[] SESSION_TOOLBAR = new StdActionCfg[]
   {
         RUN_SQL,
         NEW_SQL_TAB,
         EXEC_BOOKMARK,
         EDIT_BOOKMARK,
         RELOAD_DB_META_DATA
   };

   public static StdActionCfg[] SESSION_MENU = new StdActionCfg[]
   {
         RUN_SQL,
         NEW_SQL_TAB,
         EXEC_BOOKMARK,
         EDIT_BOOKMARK,
         RELOAD_DB_META_DATA
   };


   public static StdActionCfg[] SQL_EDITOR_RIGHT_MOUSE_MENU = new StdActionCfg[]
   {
         ESCAPE_DATE,
         SQL_TO_TABLE,
         EXEC_BOOKMARK,
         SHOW_TOOLS_POPUP,
         DUPLICATE_LINE_OR_SELECTION
   };

   private ActionCfg _actionCfg;

   StdActionCfg(String iconName, String textNonI18n, String toolsPopUpSelector, ActionScope actionScope, KeyCodeCombination keyCodeCombination)
   {
      this(new ActionCfg(getImageOrNull(iconName), new I18n(StdActionCfg.class).t(textNonI18n), toolsPopUpSelector, actionScope, keyCodeCombination));
   }

   private static Image getImageOrNull(String iconName)
   {
      if (null == iconName)
      {
         return null;
      }
      else
      {
         return new Props(StdActionCfg.class).getImage(iconName);
      }
   }

   StdActionCfg(ActionCfg actionCfg)
   {
      _actionCfg = actionCfg;
   }

   public ActionCfg getActionCfg()
   {
      return _actionCfg;
   }


   /**
    * Shortcut method to set an action for the current SessionTabContext
    * @param sqFxActionListener
    */
   public void setAction(SqFxActionListener sqFxActionListener)
   {
      _actionCfg.setAction(sqFxActionListener);
   }

}
