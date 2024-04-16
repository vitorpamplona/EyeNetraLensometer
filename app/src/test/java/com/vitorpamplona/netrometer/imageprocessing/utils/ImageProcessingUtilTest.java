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
package com.vitorpamplona.netrometer.imageprocessing.utils;

import com.vitorpamplona.netrometer.BuildConfig;

import com.vitorpamplona.netrometer.activity.CustomRobolectricGradleTestRunner;
import com.vitorpamplona.netrometer.activity.NetrometerActivity;
import com.vitorpamplona.netrometer.imageprocessing.SingleVisionImageProcessing;
import com.vitorpamplona.netrometer.imageprocessing.model.GridResult;
import com.vitorpamplona.netrometer.imageprocessing.model.Point2DGrid;
import com.vitorpamplona.netrometer.imageprocessing.model.PolarFromGrid;
import com.vitorpamplona.netrometer.imageprocessing.model.Refraction;
import com.vitorpamplona.netrometer.imageprocessing.model.VectorNeighbor;
import com.vitorpamplona.netrometer.settings.Params;
import com.vitorpamplona.netrometer.utils.Point2D;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.NaN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(CustomRobolectricGradleTestRunner.class)
@Config(sdk=21)
public class ImageProcessingUtilTest {

    /**
     * IMAGE properties.
     */
    public static final int FRAME_WIDTH = 1280;
    public static final int FRAME_HEIGHT = 720;
    public static final Point2D CENTER_POSITION = new Point2D(655.67,377.51);


    NetrometerActivity nmActivity;

    @Before
    public void setUp() {
        ActivityController<NetrometerActivity> controller = Robolectric.buildActivity(NetrometerActivity.class);
        nmActivity = controller.get();
        controller.create().resume();
    }

    /**
     * Grid positions for the NMIM001.txt.
     */
    public static final double[][] GRID_POINT_X = new double[][]{
            {490.1477,	489.22266,	489.00977,	490.15247,	490.15207,	490.1575,	490.1545,	490.0979,	490.13608,	490.32144,	490.83765,	490.16104,	491.32114,	491.4977,	491.15005,	491.00284,	491.49368,	491.50125,	491.26895,	491.00073,	491.15222,	491.26395,	491.26212},
            {504.15585,	504.1545,	504.3232,	504.15677,	504.2308,	504.9971,	504.32895,	504.45694,	504.32718,	505.49557,	505.0787,	505.118,	505.0062,	505.00687,	505.5928,	506.14536,	505.00977,	505.1561,	506.15134,	505.95792,	505.5076,	506.1522,	506.1516},
            {518.4632,	519.1066,	NaN,	    518.4632,	519.0017,	519.00616,	519.11475,	519.5032,	520.15094,	519.2685,	519.33014,	519.5928,	520.15137,	520.1498,	520.09375,	520.1585,	520.3184,	520.3436,	520.4605,	520.3237,	NaN,  	520.3148,	520.4631},
            {532.85986,	533.1209,	533.26587,	533.00903,	533.9801,	533.78455,	533.9593,	533.93365,	534.1568,	534.323,	534.1627,	534.1988,	534.31976,	534.3249,	535.1094,	534.4659,	534.4659,	535.1095,	534.3269,	535.4968,	535.1043,	535.1148,	535.00543},
            {547.9595,	548.0061,	548.158,	548.1572,	548.1555,	548.32544,	548.32306,	549.108,	548.85205,	548.99677,	548.46564,	549.4962,	549.00433,	549.00726,	549.1136,	549.1171,	549.01013,	549.2675,	550.16565,	549.0069,	550.155,	549.5053,	550.1568},
            {562.1546,	562.9998,	562.32306,	562.1372,	562.45703,	563.4981,	563.50446,	563.2886,	563.1119,	563.5038,	563.26544,	563.155,	563.26984,	563.77246,	563.5061,	564.1333,	564.3165,	564.15594,	564.22833,	564.3199,	564.3199,	564.0956,	564.3199},
            {577.2283,	577.1516,	577.26575,	576.99963,	577.1901,	577.1183,	577.01056,	577.122,	577.1085,	577.0127,	578.3162,	578.34436,	578.1273,	578.31757,	578.1532,	578.2891,	578.6864,	578.4605,	579.07733,	579.10864,	578.4591,	579.11035,	579.1103},
            {591.0047,	591.12213,	592.1506,	592.15454,	592.155,	592.1302,	592.32074,	592.15607,	592.09814,	592.3233,	592.463,	593.10913,	592.19934,	593.31976,	593.50385,	593.00323,	592.85614,	593.0058,	593.50305,	593.1163,	593.00726,	593.50305,	593.22656},
            {606.158,	605.9013,	606.15576,	606.2946,	606.3252,	606.1425,	606.4646,	606.327,	606.30273,	606.4644,	607.00146,	607.5008,	607.5,	    607.0047,	607.26935,	607.5937,	608.1492,	608.3139,	607.11957,	608.0354,	608.319,	608.2725,	608.03436},
            {620.2934,	620.1626,	621.10986,	621.00543,	620.32495,	621.5015,	621.01263,	620.4673,	621.118,	621.11597,	622.1536,	621.1203,	621.115,	622.15704,	622.3136,	622.09015,	622.1605,	622.2876,	622.3257,	622.1603,	622.2874,	623.28204,	622.97156},
            {635.0047,	635.1139,	635.1179,	636.1475,	635.2657,	635.12354,	636.14825,	635.0116,	636.15356,	636.1533,	636.3204,	636.3196,	636.1548,	636.3202,	636.324,	637.1113,	636.1612,	636.3239,	636.8556,	637.1184,	636.85596,	637.1088,	637.0038},
            {650.1526,	649.9625,	650.09485,	650.154,	650.13293,	650.158,	650.323,	650.46045,	650.15704,	650.96716,	650.46576,	650.7888,	651,    	650.8598,	651.0109,	651.1124,	651.1112,	651.50696,	651.26605,	651.1106,	652.1509,	652.1498,	651.112},
            {664.15845,	664.3248,	664.107,	664.32336,	665.00037,	665.1144,	665.1134,	665.01166,	665.1122,	665.39374,	665.0058,	665.11597,	665.1928,	665.1155,	665.2675,	665.51373,	665.124,	666.16046,	666.3199,	666.0777,	666.3208,	666.32086,	666.0405},
            {678.8581,	678.85583,	679.08185,	679.1134,	679.00684,	679.1185,	679.11804,	679.012,	680.15204,	679.12146,	680.15985,	680.15295,	680.0708,	680.15533,	680.3212,	680.3162,	680.2292,	680.3227,	680.2963,	680.9104,	680.8558,	681.00323,	680.9397},
            {693.2675,	694.3133,	693.12006,	694.155,	693.01227,	694.1518,	694.043,	694.3186,	693.96625,	694.075,	694.2928,	694.2874,	695.11475,	695.49927,	695.2288,	694.3234,	695.4085,	694.8553,	695.00397,	695.11444,	695.11835,	695.0058,	695.1232},
            {708.1511,	708.29297,	708.1545,	708.1335,	708.3221,	708.45905,	708.2359,	708.4646,	708.32635,	709.1131,	709.113,	708.8553,	709.5046,	709.5061,	709.00653,	709.1159,	709.507,	709.1198,	709.2707,	709.12103,	709.12524,	710.3195,	709.1945},
            {722.25653,	722.9997,	722.4617,	723,	    723.32025,	722.3279,	723.0671,	723.12067,	723.0057,	723.50836,	723.1198,	723.26825,	723.2681,	723.01685,	724.1497,	724.31903,	723.1176,	724.15497,	724.3196,	724.07477,	724.33954,	724.29156,	725.11035},
            {737.10724,	737.00977,	737.27606,	737.1983,	738.1504,	737.2698,	737.16235,	737.2707,	738.1547,	737.12274,	738.16,	    738.31805,	738.3216,	738.0451,	738.45905,	738.29736,	738.0788,	739.28864,	738.2972,	739.10547,	739.5037,	738.858,	739.5031},
            {751.1224,	752.1568,	751.2746,	751.9287,	752.1635,	752.15625,	751.904,	752.16046,	752.29736,	752.3201,	752.4515,	752.6917,	752.4631,	753.4072,	752.3284,	753.0003,	753.11786,	753.5037,	754.3127,	753.5031,	753.626,	753.2724,	753.27045},
            {766.1617,	766.46857,	766.5118,	766.16376,	766.166,	766.32635,	766.3327,	767.11066,	767.0069,	767.2343,	767.11945,	766.857,	767.27094,	767.6136,	767.1214,	768.3199,	767.5114,	767.1257,	768.29095,	768.0681,	768.75226,	768.3237,	768.32294},
            {781.50226,	781.1131,	NaN,	    781.11554,	781.11694,	781.00946,	780.8614,	781.11786,	781.2664,	781.27216,	781.1976,	781.2758,	782.1546,	781.11786,	782.1567,	782.29193,	782.1607,	782.19806,	782.3262,	782.3221,	NaN,  	783.16895,	783.5031},
            {795.7757,	795.1232,	796.2842,	796.15094,	795.1628,	796.157,	796.155,	796.0788,	796.16016,	796.32245,	796.0998,	797.10846,	796.3252,	797.10645,	797.4947,	796.3235,	797.11127,	797.61017,	796.8579,	797.11456,	797.6109,	797.2735,	797.95575},
            {810.326,	810.2747,	810.3226,	810.4631,	810.1049,	810.4698,	811.32135,	811.1115,	810.3304,	811.2326,	811.1148,	811.5037,	810.8611,	811.5,  	811.5061,	812.15045,	811.1209,	811.5031,	811.1243,	812.15155,	812.0957,	812.3179,	812.32196}
    };

    public static final double[][] GRID_POINT_Y = new double[][]{
            {	182.60805	,	197.4459	,	212.50215	,	226.61247	,	242.39381	,	256.40057	,	270.60934	,	285.50034	,	300.2507	,	314.5032	,	328.77496	,	344.39514	,	358.50366	,	372.90366	,	387.1556	,	402.49423	,	416.50888	,	431.50888	,	446.28848	,	460.51016	,	475.16528	,	490.2959	,	504.71933	}	,
            {	182.60286	,	198.39067	,	212.5048	,	226.60223	,	241.44548	,	256.4971	,	270.5032	,	285.15436	,	300.4968	,	314.50888	,	329.0052	,	344.16495	,	358.50436	,	372.50363	,	387.7784	,	402.39023	,	416.50943	,	431.15933	,	446.3971	,	460.67618	,	474.90665	,	490.39883	,	504.6089	}	,
            {	182.5954	,	198.15651	,	NaN	,	    226.59679	,	241.44475	,	256.4949	,	270.83234	,	285.49683	,	300.3958	,	314.71335	,	328.87814	,	343.77145	,	358.3971	,	372.614	,	    387.29956	,	402.40042	,	416.5096	,	431.00662	,	446.4061	,	460.50644	,	NaN	,	    490.49356	,	504.5985	}	,
            {	182.60893	,	198.1592	,	212.28969	,	226.50578	,	241.29161	,	256.32428	,	270.67352	,	285.00967	,	300.38635	,	314.49677	,	328.60873	,	343.8112	,	358.4968	,	372.50964	,	386.84677	,	402.40714	,	416.59705	,	430.84164	,	446.49356	,	460.5038	,	474.84732	,	490.16483	,	504.5087	}	,
            {	182.6739	,	197.4467	,	212.39922	,	226.60103	,	242.38441	,	256.4936	,	270.5016	,	284.8391	,	300.39655	,	314.49496	,	328.5988	,	344.0906	,	358.5	,	    372.50577	,	386.84375	,	402.1603	,	416.50797	,	430.7167	,	446.23718	,	460.49564	,	474.60767	,	490.09604	,	504.40378	}	,
            {	182.60283	,	197.44313	,	212.49202	,	226.74896	,	241.14827	,	256.49368	,	270.50253	,	284.8891	,	300.15894	,	314.49112	,	328.71777	,	343.15576	,	358.28873	,	372.68674	,	386.90915	,	401.58334	,	416.5	,	    430.60904	,	445.44968	,	460.5016	,	474.5096	,	489.29776	,	504.4984	}	,
            {	182.6793	,	197.15083	,	212.27972	,	226.49712	,	241.0036	,	256.16568	,	270.5	,	    284.8363	,	299.6621	,	314.49637	,	328.5032	,	343.00558	,	358.2491	,	372.5048	,	386.6128	,	401.35068	,	416.50162	,	430.59537	,	445.01318	,	460.17163	,	474.5966	,	488.8412	,	504.16653	}	,
            {	182.49785	,	196.8415	,	212.39229	,	226.59947	,	240.60765	,	256.24344	,	270.49838	,	284.60782	,	299.2964	,	314.49518	,	328.59586	,	342.84302	,	357.80872	,	372.49634	,	386.90552	,	401.50217	,	416.39468	,	430.50656	,	444.9098	,	460.16684	,	474.50943	,	488.9064	,	503.45486	}	,
            {	182.59776	,	196.84102	,	212.39058	,	226.49855	,	240.50482	,	255.57683	,	270.40744	,	284.5032	,	299.25662	,	314.40012	,	328.50217	,	342.91006	,	358.09335	,	372.49854	,	386.71567	,	401.2314	,	416.4003	,	430.50803	,	444.84183	,	459.6675	,	474.5016	,	489.04297	,	503.6688	}	,
            {	182.49712	,	196.60161	,	212.15572	,	226.50072	,	240.5	,	    256.0994	,	270.49567	,	284.59143	,	298.84256	,	314.16006	,	328.40143	,	342.8363	,	357.66794	,	372.40042	,	386.50327	,	401.29956	,	416.39755	,	430.50146	,	444.51132	,	460.3959	,	474.50143	,	488.8901	,	503.66946	}	,
            {	182.49855	,	196.83543	,	212.1565	,	226.39838	,	240.71785	,	254.84041	,	270.3936	,	284.5065	,	298.6077	,	314.39072	,	328.49838	,	342.5112	,	358.39224	,	372.4952	,	386.5112	,	400.8408	,	416.39734	,	430.50476	,	444.61356	,	460.1613	,	474.40625	,	488.8387	,	503.50272	}	,
            {	182.39247	,	196.68303	,	211.29564	,	226.39734	,	240.7531	,	254.60991	,	270.49518	,	284.58572	,	298.60797	,	313.66556	,	328.40866	,	343.26743	,	357.49783	,	372.39413	,	386.5022	,	400.83896	,	416.16174	,	430.4962	,	444.71634	,	459.66928	,	474.4065	,	488.611	,	    503.6681	}	,
            {	182.39287	,	196.5064	,	211.50346	,	226.4936	,	240.50362	,	254.8373	,	270.16315	,	284.50146	,	298.83698	,	313.83054	,	328.50146	,	342.83658	,	357.01465	,	372.1654	,	386.29605	,	400.9116	,	416.1626	,	430.4017	,	444.5096	,	459.13223	,	474.5016	,	488.51273	,	503.66928	}	,
            {	182.39655	,	196.60669	,	211.00838	,	226.16316	,	240.50362	,	254.84123	,	270.16412	,	284.4978	,	298.6069	,	314.1623	,	328.39774	,	342.6083	,	357.13217	,	372.3939	,	386.50644	,	400.51282	,	415.45435	,	430.5016	,	444.51016	,	459.30215	,	474.40738	,	488.50867	,	503.1337	}	,
            {	182.2817	,	196.4952	,	210.8437	,	226.39082	,	240.49712	,	254.60626	,	269.66446	,	284.5	,	    298.67606	,	313.12772	,	328.50146	,	342.5	,	    356.8384	,	372.09302	,	386.3202	,	400.50644	,	415.77286	,	430.39798	,	444.51163	,	458.84506	,	474.17245	,	488.5065	,	502.84534	}	,
            {	182.38713	,	196.49272	,	210.60295	,	226.24586	,	240.4952	,	254.59258	,	269.45044	,	284.40598	,	298.508	,	    312.84177	,	328.15906	,	342.60202	,	356.91006	,	372.10062	,	386.5	,	    400.8424	,	415.50317	,	430.16568	,	444.71875	,	458.84018	,	474.16965	,	488.5048	,	503.13565	}	,
            {	181.74785	,	196.49565	,	210.59706	,	225.44702	,	240.49635	,	254.5048	,	269.80768	,	284.1678	,	298.5065	,	312.9067	,	328.164	,	    342.2947	,	356.7169	,	371.5011	,	386.40652	,	400.5032	,	414.84683	,	430.3888	,	444.50797	,	459.13223	,	474.16556	,	488.50143	,	502.8412	}	,
            {	181.66187	,	196.49495	,	210.71175	,	225.1281	,	240.38887	,	254.71172	,	269.15048	,	284.28683	,	298.6054	,	312.8384	,	328.39737	,	342.4984	,	356.51126	,	371.66162	,	386.40878	,	400.5087	,	415.12964	,	430.1152	,	444.5	,	    458.84366	,	474.0991	,	488.3992	,	502.91336	}	,
            {	180.83656	,	196.39394	,	210.71503	,	225.00478	,	240.39606	,	254.59784	,	268.84555	,	284.38998	,	298.5	,	    312.51285	,	327.7167	,	342.4968	,	356.59863	,	371.77426	,	386.4952	,	400.50363	,	414.8434	,	430.10364	,	444.5048	,	458.90665	,	473.83728	,	488.3008	,	502.72116	}	,
            {	180.60603	,	196.39586	,	210.50505	,	224.61043	,	240.39134	,	254.5064	,	268.50806	,	284.16034	,	298.49854	,	312.6891	,	328.1607	,	342.39755	,	356.71115	,	371.83218	,	386.16568	,	400.5016	,	414.9061	,	430.16592	,	444.5029	,	458.74744	,	473.74878	,	488.5	,	    502.51437	}	,
            {	180.90727	,	196.15825	,	NaN	,	    224.83376	,	240.15968	,	254.50362	,	268.6013	,	284.15994	,	298.28912	,	312.70956	,	327.12802	,	342.29105	,	356.60287	,	370.8434	,	386.39584	,	400.50577	,	414.61197	,	429.80997	,	444.4968	,	458.50964	,	NaN	,	    488.2844	,	502.90674	}	,
            {	180.6866	,	196.1613	,	210.5	,	    224.60646	,	239.15251	,	254.39888	,	268.6046	,	283.12964	,	298.4016	,	312.5	,	    327.29776	,	342.16226	,	356.5	,	    370.8433	,	386.09543	,	400.49683	,	414.84424	,	429.83218	,	444.40073	,	458.83755	,	473.83444	,	488.29208	,	502.68222	}	,
            {	180.5048	,	195.04033	,	210.4984	,	224.59337	,	239.30022	,	254.40492	,	268.5024	,	282.8409	,	298.4952	,	312.6769	,	326.8436	,	342.09512	,	356.6073	,	370.9061	,	386.09512	,	400.3964	,	414.8391	,	430.0994	,	444.1675	,	458.60928	,	473.29898	,	488.5032	,	502.50803	}	,
    };

    @Test
    public void defineNeighborsResultIsNotNull() {

        assertNotNull(ImageProcessingUtil.defineNeighbors(0.5f,0.5f));
        assertNotNull(ImageProcessingUtil.defineNeighbors(2f,2f));
        assertNotNull(ImageProcessingUtil.defineNeighbors(0.5f,2f));
        assertNotNull(ImageProcessingUtil.defineNeighbors(2f,0.5f));
    }

    @Test
    public void defineNeighborsShouldBeEmpty() {

        assertTrue(ImageProcessingUtil.defineNeighbors(1f, 0.5f).size() ==0);
        assertTrue(ImageProcessingUtil.defineNeighbors(2f, 1f).size() ==0);
    }

    @Test
    public void defineNeighborsShouldNotBeEmpty() {

        assertTrue(ImageProcessingUtil.defineNeighbors(0.5f, 1f).size() >0);
        assertTrue(ImageProcessingUtil.defineNeighbors(1f, 1f).size() >0);
        assertTrue(ImageProcessingUtil.defineNeighbors(0f, 0f).size() >0);
    }

    @Test
    public void ratioToDiopterTest() {

        PolarFromGrid polarDot1 = new PolarFromGrid(100, 0,true);
        PolarFromGrid polarDot2 = new PolarFromGrid(100, Math.PI/2, true);
        PolarFromGrid polarDot3 = new PolarFromGrid(100, 2*Math.PI/2, true);
        PolarFromGrid polarDot4 = new PolarFromGrid(100, 3*Math.PI/2, true);


        List<PolarFromGrid> basePoints = new ArrayList<PolarFromGrid>();

        basePoints.add(polarDot1);
        basePoints.add(polarDot2);
        basePoints.add(polarDot3);
        basePoints.add(polarDot4);

        List<PolarFromGrid> points = basePoints;

        FittingCoefficients fit = new FittingCoefficients(Params.FITTING_COEFFICIENTS_A, Params.FITTING_COEFFICIENTS_B, Params.FITTING_COEFFICIENTS_C, Params.FITTING_COEFFICIENTS_D);


        List<Polar> diopters = ImageProcessingUtil.convertRadiiToDiopters(basePoints, points, nmActivity.getApp().getDevice());

        assertEquals(-0.458, diopters.get(0).r,.005);
        assertEquals(-0.458, diopters.get(1).r,.005);
        assertEquals(-0.458, diopters.get(2).r,.005);
        assertEquals(-0.458, diopters.get(3).r,.005);
    }

    @Test
    public void calibrationTest() {

        PolarFromGrid polarDot1 = new PolarFromGrid(100, 0,true);
        PolarFromGrid polarDot2 = new PolarFromGrid(100, Math.PI/2, true);
        PolarFromGrid polarDot3 = new PolarFromGrid(100, 2*Math.PI/2, true);
        PolarFromGrid polarDot4 = new PolarFromGrid(100, 3*Math.PI/2, true);


        List<PolarFromGrid> basePoints = new ArrayList<PolarFromGrid>();

        basePoints.add(polarDot1);
        basePoints.add(polarDot2);
        basePoints.add(polarDot3);
        basePoints.add(polarDot4);

        List<PolarFromGrid> points = basePoints;

        List<Polar> diopters = ImageProcessingUtil.convertRadiiToDiopters(basePoints, points, Params.FITTING_COEFFICIENTS_C, Params.FITTING_COEFFICIENTS_D);

        assertEquals(-0.458, diopters.get(0).r,.005);
        assertEquals(-0.458, diopters.get(1).r,.005);
        assertEquals(-0.458, diopters.get(2).r,.005);
        assertEquals(-0.458, diopters.get(3).r,.005);

        List<Polar> diopters2 = ImageProcessingUtil.convertRadiiToDiopters(basePoints, points, 0, Params.FITTING_COEFFICIENTS_D);

        assertEquals(Params.FITTING_COEFFICIENTS_D, diopters2.get(0).r,.005);
        assertEquals(Params.FITTING_COEFFICIENTS_D, diopters2.get(1).r,.005);
        assertEquals(Params.FITTING_COEFFICIENTS_D, diopters2.get(2).r,.005);
        assertEquals(Params.FITTING_COEFFICIENTS_D, diopters2.get(3).r,.005);
    }

    @Test
    public void gridToAngleTest(){

        Point2DGrid[][] point2DGrid = new Point2DGrid[GRID_POINT_X.length][GRID_POINT_X[0].length];

        for (int indX=0; indX<point2DGrid.length; indX++) {
            for (int indY = 0; indY < point2DGrid[indX].length; indY++) {
                double x = GRID_POINT_X[indX][indY];
                double y = GRID_POINT_Y[indX][indY];

                if (valid(x) && valid(y))
                    assertTrue("Given grid is outside frame boundaries X: " + x + " Y: " + y, isInsideBoundaries(x, y));

                point2DGrid[indX][indY] = new Point2DGrid(new Point2D(x, y), valid(x));
            }
        }

        GridResult grid = new GridResult(CENTER_POSITION, point2DGrid, new Point2D(Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT) );

        List<VectorNeighbor> neighbors = new ArrayList<VectorNeighbor>();
        int[][] matrixVectorXYCtrPoint = new int[][]{
                {	0	,	0	,	0	,	1	}	,
                {	0	,	0	,	-1	,	0	}	,
                {	0	,	0	,	0	,	-1	}	,
                {	0	,	0	,	1	,	0	}	,};

        for(int i=0;i<matrixVectorXYCtrPoint.length;i++) {
            VectorNeighbor vector = new VectorNeighbor();
            vector.setCenter(new Point2D(matrixVectorXYCtrPoint[i][0], matrixVectorXYCtrPoint[i][1]));
            vector.setNeighbor(new Point2D(matrixVectorXYCtrPoint[i][2], matrixVectorXYCtrPoint[i][3]));
            neighbors.add(vector);
        };

        List<PolarFromGrid> polar =  ImageProcessingUtil.convertGridToPolar(grid, neighbors, Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT);

        assertEquals(Math.PI/2, polar.get(0).theta, 0.1);
        assertEquals(Math.PI, polar.get(1).theta, 0.1);
        assertEquals(3 * Math.PI / 2, polar.get(2).theta, 1);
        assertEquals(2*Math.PI, polar.get(3).theta, 1);


    }

    @Test
    public void sinusoidToPrescriptionTest(){

        GaussNewtonSineFitting.SinusoidalModel sine1 = new GaussNewtonSineFitting.SinusoidalModel(0, 2,0*Math.PI/2, 3);
        GaussNewtonSineFitting.SinusoidalModel sine2 = new GaussNewtonSineFitting.SinusoidalModel(1, 2,1*Math.PI/2, 3);

        Refraction prescri1 = ImageProcessingUtil.convertToPrescription(sine1);
        Refraction prescri2 = ImageProcessingUtil.convertToPrescription(sine2);
        
        assertEquals(3,prescri1.dSphere,0.125);
        assertEquals(0,prescri1.dCylinder,0.125);
        assertEquals(0,prescri1.dAxis,5);

        assertEquals(4,prescri2.dSphere,0.125);
        assertEquals(-2,prescri2.dCylinder,0.125);
        assertEquals(90,prescri2.dAxis,5);



    }



    @Test
    public void findOptCtrTest() {

        double distVector = 20;
        Point2D optCtrExpected = new Point2D(GRID_POINT_X[Params.ROW_CENTER_POINT][Params.COLUMN_CENTER_POINT],GRID_POINT_Y[Params.ROW_CENTER_POINT][Params.COLUMN_CENTER_POINT]);
        Point2DGrid[][] point2DGrid = new Point2DGrid[GRID_POINT_X.length][GRID_POINT_X[0].length];
        Point2DGrid[][] point2DGridLens = new Point2DGrid[GRID_POINT_X.length][GRID_POINT_X[0].length];

        for (int indX=0; indX<point2DGrid.length; indX++) {
            for (int indY = 0; indY < point2DGrid[indX].length; indY++) {
                double x = GRID_POINT_X[indX][indY];
                double y = GRID_POINT_Y[indX][indY];

                if (valid(x) && valid(y))
                    assertTrue("Given grid is outside frame boundaries X: " + x + " Y: " + y, isInsideBoundaries(x, y));

                point2DGrid[indX][indY] = new Point2DGrid(new Point2D(x, y), valid(x));

                if(indX>Params.ROW_CENTER_POINT-3 && indX<Params.ROW_CENTER_POINT+3 && indY>Params.COLUMN_CENTER_POINT-3 && indY<Params.COLUMN_CENTER_POINT+3)
                    point2DGridLens[indX][indY] = new Point2DGrid(new Point2D(x + (indX-Params.ROW_CENTER_POINT)*distVector, y+(indY-Params.COLUMN_CENTER_POINT)*distVector), valid(x));
                else
                    point2DGridLens[indX][indY] = new Point2DGrid(new Point2D(x, y), valid(x));

            }
        }

        GridResult grid = new GridResult(CENTER_POSITION, point2DGrid, new Point2D(Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT) );
        GridResult gridLens = new GridResult(CENTER_POSITION, point2DGridLens, new Point2D(Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT) );

        Point2D optCtrFound = ImageProcessingUtil.findOpticalCtr(grid, gridLens, nmActivity.getApp().getDevice());

//        assertEquals(optCtrExpected.x, optCtrFound.x,5);
//        assertEquals(optCtrExpected.y, optCtrFound.y,5);
    }

//    @Test
//    public void histogramROITest(){
//        Clock clock = new Clock("Histogram");
//
//        Point2D refPoint = new Point2D(GRID_POINT_X[Params.ROW_CENTER_POINT][Params.COLUMN_CENTER_POINT],
//                                       GRID_POINT_Y[Params.ROW_CENTER_POINT][Params.COLUMN_CENTER_POINT]);
//
////        Rect center_BOX = new Rect(
////                (int)(refPoint.x - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
////                (int)(refPoint.y - Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
////                (int)(refPoint.x + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2),
////                (int)(refPoint.y + Params.CENTER_PROGRESSIVE_SEARCH_WIDTH/2));
//
//        Rect center_BOX = Params.CENTER_PROGRESSIVE_DISTANCE_BOX;
//
//        Point2DGrid[][] point2DGrid = new Point2DGrid[GRID_POINT_X.length][GRID_POINT_X[0].length];
//
//        for (int indX=0; indX<point2DGrid.length; indX++) {
//            for (int indY = 0; indY < point2DGrid[indX].length; indY++) {
//                double x = GRID_POINT_X[indX][indY];
//                double y = GRID_POINT_Y[indX][indY];
//
//                if (valid(x) && valid(y))
//                    assertTrue("Given grid is outside frame boundaries X: " + x + " Y: " + y, isInsideBoundaries(x, y));
//
//                point2DGrid[indX][indY] = new Point2DGrid(x, y, valid(x));
//             }
//        }
//
//        GridResult grid = new GridResult(CENTER_POSITION, point2DGrid, new Point2D(Params.ROW_CENTER_POINT, Params.COLUMN_CENTER_POINT) );
//
//        clock.capture("Setup");
//
//        RefractionStats refractionStats = ImageProcessingUtil.histogramROI(center_BOX, grid, grid);
//
//
//        assertEquals(-.5, refractionStats.getSphereStats().getPercentile(50),   .01);
//        assertEquals(0.0, refractionStats.getCylinderStats().getPercentile(50), .01);
//        assertEquals(0, refractionStats.getAxisStats().getPercentile(50), 5);
//
//
//        clock.capture("Finish");
//
//
//        System.out.println(clock.toString());
//   }

    private static boolean valid(double x) {
        return !Double.isNaN(x);
    }

    private static boolean isInsideBoundaries(double x, double y) {
        return x > 0 && y > 0 && x < FRAME_WIDTH && y < FRAME_HEIGHT;
    }


}