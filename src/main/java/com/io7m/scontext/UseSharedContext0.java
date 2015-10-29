/*
 * Copyright © 2015 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.scontext;

import com.jogamp.opengl.DebugGL3;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.valid4j.Assertive;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Objects;

public final class UseSharedContext0
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(UseSharedContext0.class);
  }

  private UseSharedContext0()
  {
    throw new AssertionError("Unreachable code!");
  }

  public static void main(
    final String[] args)
  {
    UseSharedContext0.LOG.debug("Fetching GL3 profile");

    final GLProfile pro = GLProfile.get(GLProfile.GL3);
    final GLCapabilities caps = new GLCapabilities(pro);
    caps.setFBO(true);

    UseSharedContext0.LOG.debug("Creating offscreen drawable factory");
    final GLDrawableFactory draw_fact = GLDrawableFactory.getFactory(pro);

    UseSharedContext0.LOG.debug("Creating master context");
    final GLOffscreenAutoDrawable master =
      draw_fact.createOffscreenAutoDrawable(null, caps, null, 640, 480);
    master.display();
    final GLContext master_ctx =
      Objects.requireNonNull(master.getContext(), "Master context");

    UseSharedContext0.LOG.debug("Creating slave context");
    final GLOffscreenAutoDrawable slave =
      draw_fact.createOffscreenAutoDrawable(null, caps, null, 640, 480);
    slave.setSharedAutoDrawable(master);
    slave.display();
    final GLContext slave_ctx = Objects.requireNonNull(
      slave.getContext(), "Slave context");

    LOG.debug("Checking that contexts are shared");
    Assertive.require(slave_ctx.getCreatedShares().contains(master_ctx));
    Assertive.require(master_ctx.getCreatedShares().contains(slave_ctx));

    UseSharedContext0.LOG.debug("Creating array buffer on master");

    final int buffer_id;

    {
      final IntBuffer ib = ByteBuffer.allocateDirect(4)
        .order(ByteOrder.nativeOrder())
        .asIntBuffer();

      final int r = master_ctx.makeCurrent();
      Assertive.require(r != GLContext.CONTEXT_NOT_CURRENT);

      final GL3 g = new DebugGL3(master_ctx.getGL().getGL3());
      g.glGenBuffers(1, ib);
      buffer_id = ib.get(0);
      g.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer_id);
      g.glBufferData(GL.GL_ARRAY_BUFFER, 100L, null, GL.GL_STATIC_DRAW);
      g.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
      g.glFinish();
    }

    UseSharedContext0.LOG.debug("Created array buffer on master");
    UseSharedContext0.LOG.debug("Using array buffer on slave");

    {
      final int r = slave_ctx.makeCurrent();
      Assertive.require(r != GLContext.CONTEXT_NOT_CURRENT);

      final ByteBuffer data = ByteBuffer.allocateDirect(100);

      final GL3 g = new DebugGL3(master_ctx.getGL().getGL3());
      g.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer_id);
      g.glBufferSubData(GL.GL_ARRAY_BUFFER, 0L, 100L, data);
      g.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    UseSharedContext0.LOG.debug("Destroying contexts");
    master.destroy();
    slave.destroy();
  }
}
