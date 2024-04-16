/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact me at vitor@vitorpamplona.com.
 * For AGPL licensing, see below.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This application has not been clinically tested, approved by or registered in any health agency.
 * Even though this repository grants licenses to use to any person that follow it's license,
 * any clinical or commercial use must additionally follow the laws and regulations of the
 * pertinent jurisdictions. Having a license to use the source code does not imply on having
 * regulatory approvals to use or market any part of this code.
 */
package com.vitorpamplona.netrometer.imageprocessing.hardware;

/**
 * Created by vitor on 3/4/16.
 */
public class DeviceV1011 extends AbstractDevice<DeviceV1011> {

    public DeviceV1011(long id, String what, String model, String where) {
        super(id, what, model, where);
    }

    DeviceV1011() {

    }

    public DeviceV1011 clone() {
        DeviceV1011 d = new DeviceV1011();
        copyInto(d);
        return d;
    }

    public float getThresholdNoLensLargeROI(){
        return 0.84f;
    }
    public float getThresholdNoLensCenterROI(){
        return 0.40f;
    }

    public double getDistanceNoLensCtr(){
        return 0.30;
    }
    public double getDistanceNoLensLarge(){
        return 0.50;
    }

    public float getAverageDotDistance(){
        return 14.77f;
    }

    public double getCalibrationCoefficientC(){
        return  -43.2224;
    }

    public double getCalibrationCoefficientD(){
        return 42.7644;
    }
    public double getFineTunedDiopterCoeffA(double A, double B, double C, double D, double std){
        if(A<=.135 & std>=0.13){
            A = 0;
            if (D<=-4.58 & D>-10.4){
                D=D-.25;
            } else if(D<=-3.5 & D>-4.00){
                D=D-0.05;
            }else if(D<=-4.0 & D>-4.58){
                D=D-0.15;
            } else if(D<=-10.4){
                D = D-.57;
            }
        }
        else if (A<=0.135 & std<0.13 & D<-2.62){
            A=0;
            if (D<=-4.58&D>-10.4){
                D=D-.25;
            } else if(D<=-3.5 & D>-4.00){
                D=D-0.05;
            }else if(D<=-4.0 & D>-4.58){
                D=D-0.15;
            }else if(D<=-10.4){
                D = D-.57;
            }
        }
        else if (A<=0.135 & std>0.08 & (D>=3.5 & D<6.5) ){
            A=0;
        }
        else if (A<=0.135 & std>0.05 & D>=6.5 & D<9.2){
            A=0;
            D=D+.37;
        }
        else if (A<=0.135 & std>0.05 & D>=9.2 & D< 11.0){
            A=0;
            D=D+.60;
        }
        else if (A<=0.135 & std>0.05 & D>=11.0){
            A=0;
            D=D+.8;
        }
        else if(D>-.70&D<0.65){
            D=D+0.1;
        }else if (A>0.135 & D<=-6){
            D=D-.25;
        }
        return A;
    }

    public double getFineTunedDiopterCoeffB(double A, double B, double C, double D, double std){
        return B;
    }

    public double getFineTunedDiopterCoeffC(double A, double B, double C, double D, double std){
        return C;
    }

    public double getFineTunedDiopterCoeffD(double A, double B, double C, double D, double std){
        if(A<=.135 & std>=0.13){
            A = 0;
            if (D<=-4.58 & D>-10.4){
                D=D-.25;
            } else if(D<=-3.5 & D>-4.00){
                D=D-0.05;
            }else if(D<=-4.0 & D>-4.58){
                D=D-0.15;
            } else if(D<=-10.4){
                D = D-.57;
            }
        }
        else if (A<=0.135 & std<0.13 & D<-2.62){
            A=0;
            if (D<=-4.58&D>-10.4){
                D=D-.25;
            } else if(D<=-3.5 & D>-4.00){
                D=D-0.05;
            }else if(D<=-4.0 & D>-4.58){
                D=D-0.15;
            }else if(D<=-10.4){
                D = D-.57;
            }
        }
        else if (A<=0.135 & std>0.08 & (D>=3.5 & D<6.5) ){
            A=0;
        }
        else if (A<=0.135 & std>0.05 & D>=6.5 & D<9.2){
            A=0;
            D=D+.37;
        }
        else if (A<=0.135 & std>0.05 & D>=9.2 & D< 11.0){
            A=0;
            D=D+.60;
        }
        else if (A<=0.135 & std>0.05 & D>=11.0){
            A=0;
            D=D+.8;
        }
        else if(D>-.70&D<0.65){
            D=D+0.1;
        }else if (A>0.135 & D<=-6){
            D=D-.25;
        }
        return D;
    }
}