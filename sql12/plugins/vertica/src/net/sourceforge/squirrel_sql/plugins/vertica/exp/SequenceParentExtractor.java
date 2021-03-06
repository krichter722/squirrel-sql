/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.squirrel_sql.plugins.vertica.exp;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.sourceforge.squirrel_sql.client.session.mainpanel.objecttree.expanders.ISequenceParentExtractor;
import net.sourceforge.squirrel_sql.client.session.schemainfo.ObjFilterMatcher;
import net.sourceforge.squirrel_sql.fw.sql.IDatabaseObjectInfo;

public class SequenceParentExtractor implements ISequenceParentExtractor
{
	private static final String SQL =
		"SELECT SEQUENCE_NAME FROM V_CATALOG.SEQUENCES" + 
		" WHERE SEQUENCE_SCHEMA = ? AND SEQUENCE_NAME ILIKE ?" +
        " ORDER BY SEQUENCE_NAME";
;

	@Override
	public String getSequenceParentQuery()
	{
		return SQL;
	}

	@Override
	public void bindParameters(PreparedStatement pstmt, IDatabaseObjectInfo parentDbinfo,
		ObjFilterMatcher filterMatcher) throws SQLException
	{
		pstmt.setString(1, parentDbinfo.getSchemaName());
		pstmt.setString(2, filterMatcher.getSqlLikeMatchString());
	}

}
