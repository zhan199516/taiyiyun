package com.taiyiyun.passport.commons;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nina on 2017/12/11.
 */
public class GroupPhotoDrawer {

    private Map<Integer, List<GroupPhoto>> shapeMap = new HashMap<>();

    public static void main(String[] args) {
        GroupPhotoDrawer drawer = GroupPhotoDrawer.getInstance();
        List<String> files = new ArrayList<>();
        files.add("e:\\zzzzz\\1.jpg");
        files.add("e:\\zzzzz\\2.jpg");
        files.add("e:\\zzzzz\\3.jpg");
        files.add("e:\\zzzzz\\4.jpg");
        files.add("e:\\zzzzz\\5.jpg");
        files.add("e:\\zzzzz\\6.jpg");
        files.add("e:\\zzzzz\\7.jpg");
        files.add("e:\\zzzzz\\8.jpg");
        files.add("e:\\zzzzz\\9.jpg");
//        files.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=83783821,934708634&fm=27&gp=0.jpg");
//        files.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=83783821,934708634&fm=27&gp=0.jpg");
//        files.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=83783821,934708634&fm=27&gp=0.jpg");
//        files.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=83783821,934708634&fm=27&gp=0.jpg");
//        files.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=83783821,934708634&fm=27&gp=0.jpg");
//        files.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=83783821,934708634&fm=27&gp=0.jpg");
        drawer.draw("e:\\zzzzz\\back.jpg", files, "e:\\zzzzz\\aaaa\\hehe.jpg");
    }

    private static GroupPhotoDrawer singleton = new GroupPhotoDrawer();

    private GroupPhotoDrawer() {
        shape_three();
        shape_four();
        shape_five();
        shape_six();
        shape_seven();
        shape_eight();
        shape_nine();
    }

    public void draw(String backFile, List<String> files, String groupPhotoFile) {
        try {
            BufferedImage frame=new BufferedImage(120, 120, BufferedImage.TYPE_INT_BGR);
            Graphics2D g = frame.createGraphics();
            /*背景*/
            //BufferedImage back = ImageIO.read(new File("e:\\zzzzz\\back.jpg"));
            BufferedImage back = ImageIO.read(new File(backFile));
            Image backScale = back.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            g.drawImage(backScale, 0, 0, null);
            int size = files.size();
            if(!(size >= 3 && size <= 9)) {
                throw new RuntimeException("要合成的图片个数必须为3个到9个之间！");
            }
            List<GroupPhoto> shapes = shapeMap.get(size);
            for(int i=0; i< files.size(); i++) {
                BufferedImage tmp = ImageIO.read(new File(files.get(i)));
//                URL url = new URL(files.get(i));
//                BufferedImage tmp = ImageIO.read(url.openStream());
                GroupPhoto groupPhoto = shapes.get(i);
                g.drawImage(tmp, groupPhoto.getOffset_x(), groupPhoto.getOffset_y(), groupPhoto.getWidth(), groupPhoto.getHeight(), null);
            }
            g.dispose();
            //ImageIO.write(frame, "jpg", new File("e:\\zzzzz\\hehe.jpg"));
            ImageIO.write(frame, "jpg", new File(groupPhotoFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static GroupPhotoDrawer getInstance() {return singleton;}

    private void shape_three() {
        List<GroupPhoto> shapeThree = shapeMap.get(3);
        if(shapeThree == null) {
            shapeThree = new ArrayList<>(3);
        }
        if(shapeThree.isEmpty()) {
            GroupPhoto one = new GroupPhoto();
            one.setOffset_x(31);
            one.setOffset_y(0);
            one.setWidth(57);
            one.setHeight(59);
            shapeThree.add(one);
            GroupPhoto two = new GroupPhoto();
            two.setOffset_x(2);
            two.setOffset_y(61);
            two.setWidth(57);
            two.setHeight(59);
            shapeThree.add(two);
            GroupPhoto three = new GroupPhoto();
            three.setOffset_x(61);
            three.setOffset_y(61);
            three.setWidth(57);
            three.setHeight(59);
            shapeThree.add(three);
            shapeMap.put(3, shapeThree);
        }
    }

    private void shape_four() {
        List<GroupPhoto> shapeFour = shapeMap.get(4);
        if(shapeFour == null) {
            shapeFour = new ArrayList<>(4);
        }
        if(shapeFour.isEmpty()) {
            for(int i = 0; i < 4; i++) {
                GroupPhoto gp = new GroupPhoto();
                gp.setWidth(57);
                gp.setHeight(59);
                if(i/2 < 1) {//第一行
                    gp.setOffset_y(0);
                } else {//第二行
                    gp.setOffset_y(61);
                }
                if(i%2 == 0 && i <= 2) {
                    gp.setOffset_x(2);
                } else if(i%2 > 0) {
                    gp.setOffset_x(61);
                }
                shapeFour.add(gp);
            }
            shapeMap.put(4, shapeFour);
        }
    }

    private void shape_five() {
        List<GroupPhoto> shapeFive = shapeMap.get(5);
        if(shapeFive == null) {
            shapeFive = new ArrayList<>(5);
        }
        if(shapeFive.isEmpty()) {
            for(int i = 0; i < 5; i++) {
                GroupPhoto gp = new GroupPhoto();
                gp.setWidth(37);
                if(i == 3) {
                    gp.setWidth(38);
                }
                gp.setHeight(37);
                if(i < 2) {//第一行
                    if( i == 0) {
                        gp.setOffset_x(25);
                    } else if( i == 1) {
                        gp.setOffset_x(64);
                    }
                    gp.setOffset_y(23);
                } else {//第二行
                    int offsetx = 2 * (i - 1) + gp.getWidth() * (i - 2);
                    if(i == 3) {
                        offsetx--;
                    } else if(i == 4) {
                        offsetx++;
                    }
                    int offsety = 23 + 2 + 37;
                    gp.setOffset_x(offsetx);
                    gp.setOffset_y(offsety);
                }
                shapeFive.add(gp);
            }
            shapeMap.put(5, shapeFive);
        }
    }

    private void shape_six() {
        List<GroupPhoto> shapeSix = shapeMap.get(6);
        if(shapeSix == null) {
            shapeSix = new ArrayList<>(6);
        }
        if(shapeSix.isEmpty()) {
            for(int i = 0; i < 6; i++) {
                GroupPhoto gp = new GroupPhoto();
                gp.setWidth(37);
                if(i == 1 || i == 4) {
                    gp.setWidth(38);
                }
                gp.setHeight(37);
                if(i < 3) {//第一行
                    int offsetx = (i + 1) * 2 + gp.getWidth() * i;
                    if(i == 1) {
                        offsetx--;
                    }
                    if(i == 2) {
                        offsetx++;
                    }
                    gp.setOffset_x(offsetx);
                    gp.setOffset_y(23);
                } else if(i >= 3) {//第二行
                    int offsetx = (i - 2) * 2 + gp.getWidth() * (i - 3);
                    if(i == 4) {
                        offsetx--;
                    }
                    if(i == 5) {
                        offsetx++;
                    }
                    int offsety = 23 + 2 + 37;
                    gp.setOffset_x(offsetx);
                    gp.setOffset_y(offsety);
                }
                shapeSix.add(gp);
            }
            shapeMap.put(6, shapeSix);
        }
    }

    private void shape_seven() {
        List<GroupPhoto> shapeSeven = shapeMap.get(7);
        if(shapeSeven == null) {
            shapeSeven = new ArrayList<>(7);
        }
        if(shapeSeven.isEmpty()) {
            for(int i = 0; i < 7; i++) {
                GroupPhoto gp = new GroupPhoto();
                gp.setWidth(37);
                gp.setHeight(37);
                if(i == 0 || i == 2 || i == 5) {
                    gp.setWidth(38);
                }
                if(i == 1 || i == 2 || i == 3) {
                    gp.setHeight(38);
                }
                if(i == 0) {
                    gp.setOffset_x(41);
                    gp.setOffset_y(2);
                } else if(i > 0 && i < 4) {
                    int offsetx = 2 * i + gp.getWidth() * (i - 1);
                    if(i == 2) {
                        offsetx--;
                    }
                    if(i == 3) {
                        offsetx++;
                    }
                    int offsety = 2 + 37 + 2;
                    gp.setOffset_x(offsetx);
                    gp.setOffset_y(offsety);
                } else if(i >= 4) {
                    int offsetx = 2*(i-3) + gp.getWidth() * (i-4);
                    if(i == 5) {
                        offsetx--;
                    }
                    if(i == 6) {
                        offsetx++;
                    }
                    int offsety = 2 + 37 + 2 + 38 + 2;
                    gp.setOffset_x(offsetx);
                    gp.setOffset_y(offsety);
                }
                shapeSeven.add(gp);
            }
            shapeMap.put(7, shapeSeven);
        }
    }

    private void shape_eight() {
        List<GroupPhoto> shapeEight = shapeMap.get(8);
        if(shapeEight == null) {
            shapeEight = new ArrayList<>(8);
        }
        if(shapeEight.isEmpty()) {
            for(int i = 0; i < 8; i++) {
                GroupPhoto gp = new GroupPhoto();
                gp.setWidth(37);
                gp.setHeight(37);
                if(i == 3 || i == 6) {
                    gp.setWidth(38);
                }
                if(i == 2 || i == 3 || i == 4) {
                    gp.setHeight(38);
                }
                if(i < 2) {
                    if(i == 0) {
                        gp.setOffset_x(22);
                    } else if(i == 1) {
                        gp.setOffset_x(61);
                    }
                    gp.setOffset_y(2);
                } else if(i >= 2 && i < 5) {
                    int offsetx = 2 * (i - 1) + gp.getWidth() * (i - 2);
                    if(i == 3) {
                        offsetx--;
                    }
                    if(i == 4) {
                        offsetx++;
                    }
                    gp.setOffset_x(offsetx);
                    int offsety = 2 + 37 + 2;
                    gp.setOffset_y(offsety);
                } else if(i >= 5) {
                    int offsetx = 2 * (i - 4) + gp.getWidth() * (i - 5);
                    if(i == 6) {
                        offsetx--;
                    }
                    if(i == 7) {
                        offsetx++;
                    }
                    int offsety = 2 + 37 + 2 + 38 + 2;
                    gp.setOffset_x(offsetx);
                    gp.setOffset_y(offsety);
                }
                shapeEight.add(gp);
            }
            shapeMap.put(8, shapeEight);
        }
    }

    private void shape_nine() {
        List<GroupPhoto> shapeNine = shapeMap.get(9);
        if(shapeNine == null) {
            shapeNine = new ArrayList<>(9);
        }
        if(shapeNine.isEmpty()) {
            for(int i = 0; i < 9; i++) {
                GroupPhoto gp = new GroupPhoto();
                gp.setWidth(37);
                gp.setHeight(37);
                if(i == 1 || i == 4 || i == 7) {
                    gp.setWidth(38);
                }
                if(i == 3 || i == 4 || i == 5) {
                    gp.setHeight(38);
                }
                int offsetx = 0;
                int offsety = 0;
                if(i <= 2) {
                    offsetx = 2 * (i + 1) + gp.getWidth() * i;
                    if(i == 1) {
                        offsetx--;
                    }
                    if(i == 2) {
                        offsetx++;
                    }
                    offsety = 2;
                } else if(i > 2 && i <= 5) {
                    offsetx = 2 * (i - 2) + gp.getWidth() * (i - 3);
                    if(i == 4) {
                        offsetx--;
                    }
                    if(i == 5) {
                        offsetx++;
                    }
                    offsety = 2 + 37 + 2;
                } else if(i > 5) {
                    offsetx = 2 * (i - 5) + gp.getWidth() * (i - 6);
                    if(i == 7) {
                        offsetx--;
                    }
                    if(i == 8) {
                        offsetx++;
                    }
                    offsety = 2 + 37 + 2 + 38 + 2;
                }
                gp.setOffset_x(offsetx);
                gp.setOffset_y(offsety);
                shapeNine.add(gp);
            }
            shapeMap.put(9, shapeNine);
        }
    }

    class GroupPhoto {
        private int offset_x;
        private int offset_y;
        private int width;
        private int height;

        public GroupPhoto(){};

        public int getOffset_x() {
            return offset_x;
        }

        public void setOffset_x(int offset_x) {
            this.offset_x = offset_x;
        }

        public int getOffset_y() {
            return offset_y;
        }

        public void setOffset_y(int offset_y) {
            this.offset_y = offset_y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
