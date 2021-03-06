package net.sourceforge.squirrel_sql.plugins.hibernate.viewobjects;

import net.sourceforge.squirrel_sql.plugins.hibernate.mapping.MappedClassInfo;
import net.sourceforge.squirrel_sql.plugins.hibernate.mapping.PropertyInfo;
import net.sourceforge.squirrel_sql.plugins.hibernate.server.ObjectSubstitute;

public class SingleResult implements IResult
{
   private ObjectSubstitute _object;
   private MappedClassInfo _mappedClassInfo;
   private String _toString;

   public SingleResult(ObjectSubstitute object, MappedClassInfo mappedClassInfo)
   {
      this(null, object, mappedClassInfo);
   }

   public SingleResult(String propertyNameInParent, ObjectSubstitute object, MappedClassInfo mappedClassInfo)
   {
      _object = object;
      _mappedClassInfo = mappedClassInfo;


      _toString = "";

      if(null != propertyNameInParent)
      {
         _toString = propertyNameInParent + ": ";    
      }

      _toString += _mappedClassInfo.getClassName();

      if (null == _object)
      {
         _toString += " <null>";
         return;
      }


      for (PropertyInfo propertyInfo : _mappedClassInfo.getAttributes())
      {
         if(propertyInfo.getHibernatePropertyInfo().isIdentifier())
         {
            String propertyName = propertyInfo.getHibernatePropertyInfo().getPropertyName();
            HibernatePropertyReader hpr = new HibernatePropertyReader(propertyName, _object);

            _toString += " [" + propertyName + "=" + hpr.getValue() + "; toString=\"" + _object + "\"]";
            break;
         }
      }
   }

   public ObjectSubstitute getObject()
   {
      return _object;
   }

   public MappedClassInfo getMappedClassInfo()
   {
      return _mappedClassInfo;
   }

   @Override
   public String toString()
   {
      return _toString;
   }
}
