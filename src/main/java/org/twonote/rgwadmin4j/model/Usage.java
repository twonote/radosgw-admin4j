package org.twonote.rgwadmin4j.model;

import com.google.gson.annotations.SerializedName;

public class Usage {
  @SerializedName("rgw.main")
  private RgwMain rgwMain;

  public RgwMain getRgwMain() {
    return rgwMain;
  }

  public void setRgwMain(RgwMain rgwMain) {
    this.rgwMain = rgwMain;
  }

  public static class RgwMain {
    /**
     * size : 122880 size_actual : 122880 size_utilized : 122880 size_kb : 120 size_kb_actual : 120
     * size_kb_utilized : 120 num_objects : 3
     */
    private int size;

    private int size_actual;
    private int size_utilized;
    private int size_kb;
    private int size_kb_actual;
    private int size_kb_utilized;
    private int num_objects;

    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }

    public int getSize_actual() {
      return size_actual;
    }

    public void setSize_actual(int size_actual) {
      this.size_actual = size_actual;
    }

    public int getSize_utilized() {
      return size_utilized;
    }

    public void setSize_utilized(int size_utilized) {
      this.size_utilized = size_utilized;
    }

    public int getSize_kb() {
      return size_kb;
    }

    public void setSize_kb(int size_kb) {
      this.size_kb = size_kb;
    }

    public int getSize_kb_actual() {
      return size_kb_actual;
    }

    public void setSize_kb_actual(int size_kb_actual) {
      this.size_kb_actual = size_kb_actual;
    }

    public int getSize_kb_utilized() {
      return size_kb_utilized;
    }

    public void setSize_kb_utilized(int size_kb_utilized) {
      this.size_kb_utilized = size_kb_utilized;
    }

    public int getNum_objects() {
      return num_objects;
    }

    public void setNum_objects(int num_objects) {
      this.num_objects = num_objects;
    }
  }
}
