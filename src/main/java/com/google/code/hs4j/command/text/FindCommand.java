package com.google.code.hs4j.command.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.code.hs4j.FindOperator;
import com.google.code.hs4j.command.AbstractCommand;
import com.google.code.hs4j.impl.ResultSetImpl;
import com.google.code.hs4j.network.buffer.IoBuffer;
import com.google.code.hs4j.network.hs.HandlerSocketSession;

/**
 * A find command
 * 
 * @author dennis
 * @date 2010-11-27
 */
public class FindCommand extends AbstractCommand {
	private final String id;
	private final String operator;
	private final String[] values;
	private final int limit;
	private final int offset;
	private final String[] fieldList;

	public FindCommand(String id, FindOperator operator, String[] values,
			int limit, int offset, String[] fieldList) {
		super();
		this.id = id;
		this.operator = operator.getValue();
		this.values = values;
		this.limit = limit;
		this.offset = offset;
		this.fieldList = fieldList;
	}

	@Override
	public void decodeBody(HandlerSocketSession session, IoBuffer buffer,
			int index) {
		byte[] data = new byte[index - buffer.position() + 1];
		buffer.get(data);
		List<List<byte[]>> rows = new ArrayList<List<byte[]>>(this
				.getNumColumns());
		int offset = 0;
		int cols = this.fieldList.length;
		List<byte[]> currentRow = new ArrayList<byte[]>(this.fieldList.length);
		int currentCols = 0;
		for (int i = 0; i < data.length; i++) {
			if (data[i] == TOKEN_SEPARATOR || data[i] == COMMAND_TERMINATE) {
				byte[] colData = new byte[i - offset];
				System.arraycopy(data, offset, colData, 0, colData.length);
				currentRow.add(colData);
				currentCols++;
				offset = i + 1;
				if (currentCols == cols) {
					currentCols = 0;
					rows.add(currentRow);
					currentRow = new ArrayList<byte[]>(this.fieldList.length);
				}
			}
		}
		if (!currentRow.isEmpty()) {
			rows.add(currentRow);
		}
		this.result = new ResultSetImpl(rows, this.fieldList, this.encoding);
	}

	public void encode() {
		IoBuffer buf = IoBuffer.allocate(this.id.length() + 1
				+ this.operator.length() + 1 + this.length(this.values)
				+ this.values.length + 1 + 10);

		// id
		this.writeToken(buf, this.id);
		this.writeTokenSeparator(buf);
		// operator
		this.writeToken(buf, this.operator);
		this.writeTokenSeparator(buf);
		// value nums
		this.writeToken(buf, String.valueOf(this.values.length));
		this.writeTokenSeparator(buf);
		for (String key : this.values) {
			this.writeToken(buf, key);
			this.writeTokenSeparator(buf);
		}
		// limit
		this.writeToken(buf, String.valueOf(this.limit));
		this.writeTokenSeparator(buf);
		// offset
		this.writeToken(buf, String.valueOf(this.offset));
		this.writeCommandTerminate(buf);

		buf.flip();
		System.out.println(Arrays.toString(buf.array()));
		this.buffer = buf;
	}

}
