package com.google.code.hs4j.network.hs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.hs4j.Command;
import com.google.code.hs4j.HSClientStateListener;
import com.google.code.hs4j.impl.HSClientImpl;
import com.google.code.hs4j.network.core.Session;
import com.google.code.hs4j.network.core.impl.HandlerAdapter;

/**
 * HandlerSocket io event handler
 * 
 * @author dennis
 * @date 2010-11-27
 */
public class HandlerSocketHandler extends HandlerAdapter {
	static final Logger log = LoggerFactory
			.getLogger(HandlerSocketHandler.class);

	private final HSClientImpl hsClient;

	public HandlerSocketHandler(HSClientImpl hsClient) {
		super();
		this.hsClient = hsClient;
	}

	/**
	 * put command which have been sent to queue
	 */
	@Override
	public final void onMessageSent(Session session, Object msg) {
		Command command = (Command) msg;
		((HandlerSocketSession) session).addCommand(command);

	}

	@Override
	public void onExceptionCaught(Session session, Throwable throwable) {
		log.error("XMemcached network layout exception", throwable);
	}

	/**
	 * Check if have to reconnect on session closed
	 */
	@Override
	public final void onSessionClosed(Session session) {
		this.hsClient.getConnector().removeSession(session);
		HandlerSocketSession memcachedSession = (HandlerSocketSession) session;
		// destroy memached session
		memcachedSession.destroy();
		// if (client.getConnector().isStarted()
		// && memcachedSession.isAllowReconnect()) {
		// reconnect(session);
		// }
		// TOOD 处理重连
		for (HSClientStateListener listener : this.hsClient
				.getHSClientStateListeners()) {
			listener.onDisconnected(this.hsClient, session
					.getRemoteSocketAddress());
		}
	}

}
