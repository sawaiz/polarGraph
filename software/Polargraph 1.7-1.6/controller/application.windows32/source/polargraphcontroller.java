import processing.core.*; 
import processing.xml.*; 

import diewald_CV_kit.libraryinfo.*; 
import diewald_CV_kit.utility.*; 
import diewald_CV_kit.blobdetection.*; 
import geomerative.*; 
import org.apache.batik.svggen.font.table.*; 
import org.apache.batik.svggen.font.*; 
import java.util.zip.CRC32; 
import java.text.*; 
import java.util.*; 
import java.io.*; 
import java.util.logging.*; 
import javax.swing.*; 
import processing.serial.*; 
import controlP5.*; 
import java.awt.event.*; 
import java.awt.Toolkit; 
import java.awt.BorderLayout; 
import java.awt.GraphicsEnvironment; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class polargraphcontroller extends PApplet {

/**
  Polargraph controller
  Copyright Sandy Noble 2013.

  This file is part of Polargraph Controller.

  Polargraph Controller is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Polargraph Controller is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
    
  Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
  Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
  
  This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.

  sandy.noble@gmail.com
  http://www.polargraph.co.uk/
  http://code.google.com/p/polargraph/

*/
//import processing.video.*;










// for OSX










int majorVersionNo = 1;
int minorVersionNo = 6;
int buildNo = 0;

String programTitle = "Polargraph Controller v" + majorVersionNo + "." + minorVersionNo + " build " + buildNo;
ControlP5 cp5;

boolean drawbotReady = false;
boolean drawbotConnected = false;

static final int HARDWARE_VER_UNO = 1;
static final int HARDWARE_VER_MEGA = 100;
static final int HARDWARE_VER_MEGA_POLARSHIELD = 200;
int currentHardware = HARDWARE_VER_MEGA_POLARSHIELD;

final int HARDWARE_ATMEGA328_SRAM = 2048;
final int HARDWARE_ATMEGA1280_SRAM = 8096;
int currentSram = HARDWARE_ATMEGA328_SRAM;

String newMachineName = "PGXXABCD";
PVector machinePosition = new PVector(130.0f, 50.0f);
float machineScaling = 1.0f;
DisplayMachine displayMachine = null;

int homeALengthMM = 400;
int homeBLengthMM = 400;

// preset sizes - these can be referred to in the properties file
// and will be automatically converted to numbers when loaded.
final String PRESET_A3_SHORT = "A3SHORT";
final String PRESET_A3_LONG = "A3LONG";
final String PRESET_A2_SHORT = "A2SHORT";
final String PRESET_A2_LONG = "A2LONG";
final String PRESET_A2_IMP_SHORT = "A2+SHORT";
final String PRESET_A2_IMP_LONG = "A2+LONG";
final String PRESET_A1_SHORT = "A1SHORT";
final String PRESET_A1_LONG = "A1LONG";

final int A3_SHORT = 297;
final int A3_LONG = 420;
final int A2_SHORT = 418;
final int A2_LONG = 594;
final int A2_IMP_SHORT = 450;
final int A2_IMP_LONG = 640;
final int A1_SHORT = 594;
final int A1_LONG = 841;

int leftEdgeOfQueue = 800;
int rightEdgeOfQueue = 1100;
int topEdgeOfQueue = 0;
int bottomEdgeOfQueue = 0;
int queueRowHeight = 15;

int baudRate = 57600;
Serial myPort;                       // The serial port
int[] serialInArray = new int[1];    // Where we'll put what we receive
int serialCount = 0;                 // A count of how many bytes we receive

boolean[] keys = new boolean[526];

final JFileChooser chooser = new JFileChooser();

SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm:ss");

String commandStatus = "Waiting for a click.";

float sampleArea = 10;
float gridSize = 75.0f;
float currentPenWidth = 0.8f;
float penIncrement = 0.05f;

int penLiftDownPosition = 90;
int penLiftUpPosition = 180;
// this variable controls how big the pixels are scaled when drawn.
// 1.0 represents full size, 2.0 would be twice as big as the grid size,
// 0.5 would be half the grid size.
float pixelScalingOverGridSize = 1.0f;

float currentMachineMaxSpeed = 600.0f;
float currentMachineAccel = 400.0f;
float MACHINE_ACCEL_INCREMENT = 25.0f;
float MACHINE_MAXSPEED_INCREMENT = 25.0f;

List<String> commandQueue = new ArrayList<String>();
List<String> realtimeCommandQueue = new ArrayList<String>();
List<String> commandHistory = new ArrayList<String>();

List<PreviewVector> previewCommandList = new ArrayList<PreviewVector>();
long lastCommandQueueHash = 0L;


String lastCommand = "";
String lastDrawingCommand = "";
Boolean commandQueueRunning = false;
static final int DRAW_DIR_NE = 1;
static final int DRAW_DIR_SE = 2;
static final int DRAW_DIR_SW = 3;
static final int DRAW_DIR_NW = 4;
static final int DRAW_DIR_N = 5;
static final int DRAW_DIR_E = 6;
static final int DRAW_DIR_S = 7;
static final int DRAW_DIR_W = 8;

static final int DRAW_DIR_MODE_AUTO = 1;
static final int DRAW_DIR_MODE_PRESET = 2;
static final int DRAW_DIR_MODE_RANDOM = 3;
static int pixelDirectionMode = DRAW_DIR_MODE_PRESET;

static final int PIXEL_STYLE_SQ_FREQ = 0;
static final int PIXEL_STYLE_SQ_SIZE = 1;
static final int PIXEL_STYLE_SQ_SOLID = 2;
static final int PIXEL_STYLE_SCRIBBLE = 3;
static final int PIXEL_STYLE_CIRCLE = 4;
static final int PIXEL_STYLE_SAW = 5;


PVector currentMachinePos = new PVector();
PVector currentCartesianMachinePos = new PVector();
int machineAvailMem = 0;
int machineUsedMem = 0;
int machineMinAvailMem = 2048;


//String testPenWidthCommand = "TESTPENWIDTHSCRIBBLE,";
String testPenWidthCommand = CMD_TESTPENWIDTHSQUARE;
float testPenWidthStartSize = 0.5f;
float testPenWidthEndSize = 2.0f;
float testPenWidthIncrementSize = 0.5f;

int machineStepMultiplier = 1;

int maxSegmentLength = 2;

static final String MODE_BEGIN = "button_mode_begin";
static final String MODE_DRAW_OUTLINE_BOX = "button_mode_drawOutlineBox";
static final String MODE_DRAW_OUTLINE_BOX_ROWS = "button_mode_drawOutlineBoxRows";
static final String MODE_DRAW_SHADE_BOX_ROWS_PIXELS = "button_mode_drawShadeBoxRowsPixels";
static final String MODE_RENDER_SQUARE_PIXELS = "button_mode_renderSquarePixel";
static final String MODE_RENDER_SAW_PIXELS = "button_mode_renderSawPixel";
static final String MODE_RENDER_CIRCLE_PIXELS = "button_mode_renderCirclePixel";
static final String MODE_RENDER_PIXEL_DIALOG = "button_mode_drawPixelsDialog";

static final String MODE_INPUT_ROW_START = "button_mode_inputRowStart";
static final String MODE_INPUT_ROW_END = "button_mode_inputRowEnd";
static final String MODE_DRAW_TESTPATTERN = "button_mode_drawTestPattern";
static final String MODE_DRAW_GRID = "button_mode_drawGrid";
static final String MODE_PLACE_IMAGE = "button_mode_placeImage";
static final String MODE_LOAD_IMAGE = "button_mode_loadImage";
static final String MODE_PAUSE_QUEUE = "button_mode_pauseQueue";
static final String MODE_RUN_QUEUE = "button_mode_runQueue";
static final String MODE_SET_POSITION_HOME = "button_mode_setPositionHome";
static final String MODE_RETURN_TO_HOME = "button_mode_returnToHome";
static final String MODE_INPUT_SINGLE_PIXEL = "button_mode_inputSinglePixel";
static final String MODE_DRAW_TEST_PENWIDTH = "button_mode_drawTestPenWidth";
static final String MODE_RENDER_SCALED_SQUARE_PIXELS = "button_mode_renderScaledSquarePixels";
static final String MODE_RENDER_SOLID_SQUARE_PIXELS = "button_mode_renderSolidSquarePixels";
static final String MODE_RENDER_SCRIBBLE_PIXELS = "button_mode_renderScribblePixels";
static final String MODE_CHANGE_MACHINE_SPEC = "button_mode_changeMachineSpec";
static final String MODE_REQUEST_MACHINE_SIZE = "button_mode_requestMachineSize";
static final String MODE_RESET_MACHINE = "button_mode_resetMachine";

static final String MODE_SAVE_PROPERTIES = "button_mode_saveProperties";
static final String MODE_SAVE_AS_PROPERTIES = "button_mode_saveAsProperties";
static final String MODE_LOAD_PROPERTIES = "button_mode_loadProperties";

static final String MODE_INC_SAMPLE_AREA = "button_mode_incSampleArea";
static final String MODE_DEC_SAMPLE_AREA = "button_mode_decSampleArea";
static final String MODE_INPUT_IMAGE = "button_mode_inputImage";
static final String MODE_IMAGE_PIXEL_BRIGHT_THRESHOLD = "numberbox_mode_pixelBrightThreshold";
static final String MODE_IMAGE_PIXEL_DARK_THRESHOLD = "numberbox_mode_pixelDarkThreshold";

static final String MODE_CONVERT_BOX_TO_PICTUREFRAME = "button_mode_convertBoxToPictureframe";
static final String MODE_SELECT_PICTUREFRAME = "button_mode_selectPictureframe";
static final String MODE_EXPORT_QUEUE = "button_mode_exportQueue";
static final String MODE_IMPORT_QUEUE = "button_mode_importQueue";
static final String MODE_CLEAR_QUEUE = "button_mode_clearQueue";
static final String MODE_FIT_IMAGE_TO_BOX = "button_mode_fitImageToBox";
static final String MODE_RESIZE_IMAGE = "numberbox_mode_resizeImage";
static final String MODE_RENDER_COMMAND_QUEUE = "button_mode_renderCommandQueue";

static final String MODE_MOVE_IMAGE = "toggle_mode_moveImage";
static final String MODE_SET_POSITION = "toggle_mode_setPosition";
static final String MODE_INPUT_BOX_TOP_LEFT = "toggle_mode_inputBoxTopLeft";
static final String MODE_INPUT_BOX_BOT_RIGHT = "toggle_mode_inputBoxBotRight";
static final String MODE_DRAW_TO_POSITION = "toggle_mode_drawToPosition";
static final String MODE_DRAW_DIRECT = "toggle_mode_drawDirect";

static final String MODE_CHANGE_SAMPLE_AREA = "numberbox_mode_changeSampleArea";
static final String MODE_CHANGE_GRID_SIZE = "numberbox_mode_changeGridSize";

static final String MODE_SHOW_DENSITY_PREVIEW = "minitoggle_mode_showDensityPreview";
static final String MODE_SHOW_IMAGE = "minitoggle_mode_showImage";
static final String MODE_SHOW_QUEUE_PREVIEW = "minitoggle_mode_showQueuePreview";
static final String MODE_SHOW_VECTOR = "minitoggle_mode_showVector";
static final String MODE_SHOW_GUIDES = "minitoggle_mode_showGuides";

static final String MODE_CHANGE_MACHINE_WIDTH = "numberbox_mode_changeMachineWidth";
static final String MODE_CHANGE_MACHINE_HEIGHT = "numberbox_mode_changeMachineHeight";
static final String MODE_CHANGE_MM_PER_REV = "numberbox_mode_changeMMPerRev";
static final String MODE_CHANGE_STEPS_PER_REV = "numberbox_mode_changeStepsPerRev";
static final String MODE_CHANGE_STEP_MULTIPLIER = "numberbox_mode_changeStepMultiplier";
static final String MODE_CHANGE_PAGE_WIDTH = "numberbox_mode_changePageWidth";
static final String MODE_CHANGE_PAGE_HEIGHT = "numberbox_mode_changePageHeight";
static final String MODE_CHANGE_PAGE_OFFSET_X = "numberbox_mode_changePageOffsetX";
static final String MODE_CHANGE_PAGE_OFFSET_Y = "numberbox_mode_changePageOffsetY";
static final String MODE_CHANGE_PAGE_OFFSET_X_CENTRE = "button_mode_changePageOffsetXCentre";

static final String MODE_CHANGE_HOMEPOINT_X = "numberbox_mode_changeHomePointX";
static final String MODE_CHANGE_HOMEPOINT_Y = "numberbox_mode_changeHomePointY";
static final String MODE_CHANGE_HOMEPOINT_X_CENTRE = "button_mode_changeHomePointXCentre";

static final String MODE_CHANGE_PEN_WIDTH = "numberbox_mode_changePenWidth";
static final String MODE_SEND_PEN_WIDTH = "button_mode_sendPenWidth";

static final String MODE_CHANGE_PEN_TEST_START_WIDTH = "numberbox_mode_changePenTestStartWidth";
static final String MODE_CHANGE_PEN_TEST_END_WIDTH = "numberbox_mode_changePenTestEndWidth";
static final String MODE_CHANGE_PEN_TEST_INCREMENT_SIZE = "numberbox_mode_changePenTestIncrementSize";

static final String MODE_CHANGE_MACHINE_MAX_SPEED = "numberbox_mode_changeMachineMaxSpeed";
static final String MODE_CHANGE_MACHINE_ACCELERATION = "numberbox_mode_changeMachineAcceleration";
static final String MODE_SEND_MACHINE_SPEED = "button_mode_sendMachineSpeed";
static final String MODE_SEND_MACHINE_SPEED_PERSIST = "button_mode_sendMachineSpeedPersist";

static final String MODE_RENDER_VECTORS = "button_mode_renderVectors";
static final String MODE_LOAD_VECTOR_FILE = "button_mode_loadVectorFile";
static final String MODE_CHANGE_MIN_VECTOR_LINE_LENGTH = "numberbox_mode_changeMinVectorLineLength";

static final String MODE_CHANGE_SERIAL_PORT = "button_mode_serialPortDialog";
static final String MODE_SEND_MACHINE_STORE_MODE = "button_mode_machineStoreDialog";
static final String MODE_SEND_MACHINE_LIVE_MODE = "button_mode_sendMachineLiveMode";
static final String MODE_SEND_MACHINE_EXEC_MODE = "button_mode_machineExecDialog";

static final String MODE_RESIZE_VECTOR = "numberbox_mode_resizeVector";
static final String MODE_MOVE_VECTOR = "toggle_mode_moveVector";

static final String MODE_CHOOSE_CHROMA_KEY_COLOUR = "toggle_mode_chooseChromaKeyColour";
static final String MODE_CHANGE_PIXEL_SCALING = "numberbox_mode_changePixelScaling";
static final String MODE_PEN_LIFT_UP = "button_mode_penUp";
static final String MODE_PEN_LIFT_DOWN = "button_mode_penDown";
static final String MODE_PEN_LIFT_POS_UP = "numberbox_mode_penUpPos";
static final String MODE_PEN_LIFT_POS_DOWN = "numberbox_mode_penDownPos";
static final String MODE_SEND_PEN_LIFT_RANGE = "button_mode_sendPenliftRange";
static final String MODE_SEND_PEN_LIFT_RANGE_PERSIST = "button_mode_sendPenliftRangePersist";

static final String MODE_SEND_ROVE_AREA = "button_mode_sendRoveArea";
static final String MODE_SELECT_ROVE_IMAGE_SOURCE = "button_mode_selectRoveImageSource";
static final String MODE_SEND_START_TEXT = "toggle_mode_sendStartText";
// controls to do with text start
static final String MODE_CHANGE_TEXT_ROW_SIZE = "numberbox_mode_changeTextRowSize";
static final String MODE_CHANGE_TEXT_ROW_SPACING = "numberbox_mode_changeTextRowSize";

static final String MODE_SHOW_WRITING_DIALOG = "button_mode_drawWritingDialog";
static final String MODE_START_SWIRLING = "button_mode_startSwirling";
static final String MODE_STOP_SWIRLING = "button_mode_stopSwirling";
static final String MODE_START_MARKING = "button_mode_startMarking";
static final String MODE_STOP_MARKING = "button_mode_stopMarking";
static final String MODE_START_SPRITE = "button_mode_drawSpriteDialog";
static final String MODE_START_RANDOM_SPRITES = "button_mode_startRandomSprite";
static final String MODE_STOP_RANDOM_SPRITES = "button_mode_stopRandomSprites";
static final String MODE_DRAW_NORWEGIAN_DIALOG = "button_mode_drawNorwegianDialog";

static final String MODE_LIVE_BLUR_VALUE = "numberbox_mode_liveBlurValue";
static final String MODE_LIVE_SIMPLIFICATION_VALUE = "numberbox_mode_liveSimplificationValue";
static final String MODE_LIVE_POSTERISE_VALUE = "numberbox_mode_livePosteriseValue";
static final String MODE_LIVE_CAPTURE_FROM_LIVE = "button_mode_liveCaptureFromLive";
static final String MODE_LIVE_CANCEL_CAPTURE = "button_mode_liveClearCapture";
static final String MODE_LIVE_ADD_CAPTION = "button_mode_liveAddCaption";
static final String MODE_LIVE_CONFIRM_DRAW = "button_mode_liveConfirmDraw";

static final String MODE_VECTOR_PATH_LENGTH_HIGHPASS_CUTOFF = "numberbox_mode_vectorPathLengthHighPassCutoff";
static final String MODE_SHOW_WEBCAM_RAW_VIDEO = "toggle_mode_showWebcamRawVideo";
static final String MODE_FLIP_WEBCAM_INPUT = "toggle_mode_flipWebcam";
static final String MODE_ROTATE_WEBCAM_INPUT = "toggle_mode_rotateWebcam";


PVector statusTextPosition = new PVector(300.0f, 12.0f);

static String currentMode = MODE_BEGIN;
static String lastMode = MODE_BEGIN;

static PVector boxVector1 = null;
static PVector boxVector2 = null;

static PVector rowsVector1 = null;
static PVector rowsVector2 = null;

static final float MASKED_PIXEL_BRIGHTNESS = -1.0f;
static int pixelExtractBrightThreshold = 255;
static int pixelExtractDarkThreshold = 0;
static boolean liftPenOnMaskedPixels = true;
int numberOfPixelsTotal = 0;
int numberOfPixelsCompleted = 0;

Date timerStart = null;
Date timeLastPixelStarted = null;

boolean pixelTimerRunning = false;
boolean displayingSelectedCentres = false;
boolean displayingRowGridlines = false;
boolean displayingInfoTextOnInputPage = false;

boolean displayingImage = true;
boolean displayingVector = true;
boolean displayingQueuePreview = true;
boolean displayingDensityPreview = false;

boolean displayingGuides = true;

static final int DENSITY_PREVIEW_ROUND = 0;
static final int DENSITY_PREVIEW_DIAMOND = 1;
static final int DEFAULT_DENSITY_PREVIEW_STYLE = DENSITY_PREVIEW_DIAMOND;
int densityPreviewStyle = DEFAULT_DENSITY_PREVIEW_STYLE;

static final byte COORD_MODE_NATIVE_STEPS = 0;
static final byte COORD_MODE_NATIVE_MM = 1;
static final byte COORD_MODE_CARTESIAN_MM_ABS = 2;
static final byte COORD_MODE_CARTESIAN_MM_SCALED = 3;


boolean useSerialPortConnection = false;

static final char BITMAP_BACKGROUND_COLOUR = 0x0F;

PVector homePointCartesian = null;

public int chromaKeyColour = color(0,255,0);

// used in the preview page
public int pageColour = color(220);
public int frameColour = color(200,0,0);
public int machineColour = color(150);
public int guideColour = color(255);
public int backgroundColour = color(100);
public int densityPreviewColour = color(0);


public boolean showingSummaryOverlay = true;
public boolean showingDialogBox = false;

public Integer windowWidth = 650;
public Integer windowHeight = 400;

public static Integer serialPortNumber = -1;


Properties props = null;
public static String propertiesFilename = "default.properties.txt";
public static String newPropertiesFilename = null;

public static final String TAB_NAME_INPUT= "default";
public static final String TAB_LABEL_INPUT = "input";
public static final String TAB_NAME_ROVING = "tab_roving";
public static final String TAB_LABEL_ROVING = "Roving";
public static final String TAB_NAME_DETAILS = "tab_details";
public static final String TAB_LABEL_DETAILS = "Setup";
public static final String TAB_NAME_QUEUE = "tab_queue";
public static final String TAB_LABEL_QUEUE = "Queue";
public static final String TAB_NAME_TRACE = "tab_trace";
public static final String TAB_LABEL_TRACE = "Trace";

// Page states
public String currentTab = TAB_NAME_INPUT;


public static final String PANEL_NAME_INPUT = "panel_input";
public static final String PANEL_NAME_ROVING = "panel_roving";
public static final String PANEL_NAME_DETAILS = "panel_details";
public static final String PANEL_NAME_QUEUE = "panel_queue";
public static final String PANEL_NAME_TRACE = "panel_trace";

public static final String PANEL_NAME_GENERAL = "panel_general";

public final PVector DEFAULT_CONTROL_SIZE = new PVector(100.0f, 20.0f);
public final PVector CONTROL_SPACING = new PVector(2.0f, 2.0f);
public PVector mainPanelPosition = new PVector(10.0f, 85.0f);

public final Integer PANEL_MIN_HEIGHT = 400;

public Set<String> panelNames = null;
public List<String> tabNames = null;
public Set<String> controlNames = null;
public Map<String, List<Controller>> controlsForPanels = null;

public Map<String, Controller> allControls = null;
public Map<String, String> controlLabels = null;
public Set<String> controlsToLockIfBoxNotSpecified = null;
public Set<String> controlsToLockIfImageNotLoaded = null;

public Map<String, Set<Panel>> panelsForTabs = null;
public Map<String, Panel> panels = null;

// machine moving
PVector machineDragOffset = new PVector (0.0f, 0.0f);
PVector lastMachineDragPosition = new PVector (0.0f, 0.0f);
public final float MIN_SCALING = 0.1f;
public final float MAX_SCALING = 5.0f;

RShape vectorShape = null;
String vectorFilename = null;
float vectorScaling = 100;
PVector vectorPosition = new PVector(0.0f,0.0f);
int minimumVectorLineLength = 0;
public static final int VECTOR_FILTER_LOW_PASS = 0;


String storeFilename = "comm.txt";
boolean overwriteExistingStoreFile = true;
//private static Logger logger;
public static Console console;
public boolean useWindowedConsole = false;

static boolean drawingTraceShape = true;
static boolean retraceShape = true;
static boolean flipWebcamImage = false;
static boolean rotateWebcamImage = false;
static boolean confirmedDraw = false;

static PImage liveImage = null;
static PImage processedLiveImage = null;
static PImage capturedImage = null;
static PImage processedCapturedImage = null;

static final Integer LIVE_SIMPLIFICATION_MIN = 1;
static final Integer LIVE_SIMPLIFICATION_MAX = 32;

static int pathLengthHighPassCutoff = 0;
static final Integer PATH_LENGTH_HIGHPASS_CUTOFF_MAX = 10000;
static final Integer PATH_LENGTH_HIGHPASS_CUTOFF_MIN = 0;

//Capture liveCamera;
//JMyron liveCamera;
BlobDetector blob_detector;
int liveSimplification = 5;
int blurValue = 1;
int posterizeValue = 5;
int sepKeyColour = color(0, 0, 255);

Map<Integer, PImage> colourSeparations = null;
RShape traceShape = null;
RShape captureShape = null;

String shapeSavePath = "../../savedcaptures/";
String shapeSavePrefix = "shape-";
String shapeSaveExtension = ".svg";

//boolean displayGamepadOverlay = false;
//PImage yButtonImage = null;
//PImage xButtonImage = null;
//PImage aButtonImage = null;
//PImage bButtonImage = null;
//
//PImage dpadXImage = null;
//PImage dpadYImage = null;

public void setup()
{
  println("Running polargraph controller");
  frame.setResizable(true);
  initLogging();
  
  initImages();
  
  RG.init(this);
  RG.setPolygonizer(RG.ADAPTATIVE);

  try 
  { 
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
  } 
  catch (Exception e) 
  { 
    e.printStackTrace();   
  }
  loadFromPropertiesFile();
  
  this.cp5 = new ControlP5(this);
  initTabs();

  String[] serialPorts = Serial.list();
  println("Serial ports available on your machine:");
  println(serialPorts);

//  println("getSerialPortNumber()"+getSerialPortNumber());
  if (getSerialPortNumber() >= 0)
  {
    println("About to connect to serial port in slot " + getSerialPortNumber());
    // Print a list of the serial ports, for debugging purposes:
    if (serialPorts.length > 0)
    {
      String portName = null;
      try 
      {
        println("Get serial port no: "+getSerialPortNumber());
        portName = serialPorts[getSerialPortNumber()];
        myPort = new Serial(this, portName, getBaudRate());
        //read bytes into a buffer until you get a linefeed (ASCII 10):
        myPort.bufferUntil('\n');
        useSerialPortConnection = true;
        println("Successfully connected to port " + portName);
      }
      catch (Exception e)
      {
        println("Attempting to connect to serial port " 
        + portName + " in slot " + getSerialPortNumber() 
        + " caused an exception: " + e.getMessage());
      }
    }
    else
    {
      println("No serial ports found.");
      useSerialPortConnection = false;
    }
  }
  else
  {
    useSerialPortConnection = false;
  }

  currentMode = MODE_BEGIN;
  preLoadCommandQueue();
  size(windowWidth, windowHeight, JAVA2D );
  changeTab(TAB_NAME_INPUT, TAB_NAME_INPUT);

  addEventListeners();

  //gamepad_init();
}
public void addEventListeners()
{
  frame.addComponentListener(new ComponentAdapter() 
    {
      public void componentResized(ComponentEvent event) 
      {
        if (event.getSource()==frame) 
        {
  	  windowResized();
        }
      }
    }
  ); 
  addMouseWheelListener(new java.awt.event.MouseWheelListener() 
    { 
      public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) 
      { 
        mouseWheel(evt.getWheelRotation());
      }
    }
  ); 
}  


public void preLoadCommandQueue()
{
  addToCommandQueue(CMD_CHANGEPENWIDTH+currentPenWidth+",END");
  addToCommandQueue(CMD_SETMOTORSPEED+currentMachineMaxSpeed+",END");
  addToCommandQueue(CMD_SETMOTORACCEL+currentMachineAccel+",END");
}

public void windowResized()
{
  windowWidth = frame.getWidth();
  windowHeight = frame.getHeight();
  for (String key : getPanels().keySet())
  {
    Panel p = getPanels().get(key);
    p.setHeight(frame.getHeight() - p.getOutline().getTop() - (DEFAULT_CONTROL_SIZE.y*2));
  }
  
}
public void draw()
{
  if (getCurrentTab() == TAB_NAME_INPUT)
  {
    drawImagePage();
  }
  else if (getCurrentTab() == TAB_NAME_QUEUE)
  {
    drawCommandQueuePage();
  }
  else if (getCurrentTab() == TAB_NAME_DETAILS)
  {
    drawDetailsPage();
  }
  else if (getCurrentTab() == TAB_NAME_ROVING)
  {
    drawRovingPage();
  }
  else if (getCurrentTab() == TAB_NAME_TRACE)
  {
    drawTracePage();
  }
  else
  {
    drawDetailsPage();
  }


  if (isShowingSummaryOverlay())
  {
    drawSummaryOverlay();
  }
  if (isShowingDialogBox())
  {
    drawDialogBox();
  }

  if (drawbotReady)
  {
    dispatchCommandQueue();
  }
  
}

public String getCurrentTab()
{
  return this.currentTab;
}

public boolean isShowingSummaryOverlay()
{
  return this.showingSummaryOverlay;
}
public void drawSummaryOverlay()
{
}
public boolean isShowingDialogBox()
{
  return false;
}
public void drawDialogBox()
{
  
}
public String getVectorFilename()
{
  return this.vectorFilename;
}
public void setVectorFilename(String filename)
{
  this.vectorFilename = filename;
}
public RShape getVectorShape()
{
  return this.vectorShape;
}
public void setVectorShape(RShape shape)
{
  this.vectorShape = shape;
}

public int getPageColour()
{
  return this.pageColour;
}
public int getMachineColour()
{
  return this.machineColour;
}
public int getBackgroundColour()
{
  return this.backgroundColour;
}
public int getGuideColour()
{
  return this.guideColour;
}
public int getFrameColour()
{
  return this.frameColour;
}


public Panel getPanel(String panelName)
{
  return getPanels().get(panelName);
}

public void drawImagePage()
{
  strokeWeight(1);
  background(getBackgroundColour());
  noFill();
  stroke(255, 150, 255, 100);
  strokeWeight(3);
  stroke(150);
  noFill();
  getDisplayMachine().draw();
  drawMoveImageOutline();
  stroke(255, 0, 0);
 
  for (Panel panel : getPanelsForTab(TAB_NAME_INPUT))
  {
    panel.draw();
  }
  stroke(200,200);
  text(propertiesFilename, getPanel(PANEL_NAME_GENERAL).getOutline().getLeft(), getPanel(PANEL_NAME_GENERAL).getOutline().getTop()-7);

  showGroupBox();
  showCurrentMachinePosition();
  if (displayingQueuePreview)
    previewQueue();
  if (displayingInfoTextOnInputPage)
    showText(250,45);
  drawStatusText((int)statusTextPosition.x, (int)statusTextPosition.y);

  showCommandQueue((int) getDisplayMachine().getOutline().getRight()+6, 20);
}

public void drawMachineOutline()
{
  rect(machinePosition.x,machinePosition.y, machinePosition.x+getDisplayMachine().getWidth(), machinePosition.y+getDisplayMachine().getHeight());
}
public void drawDetailsPage()
{
  strokeWeight(1);
  background(100);
  noFill();
  stroke(255, 150, 255, 100);
  strokeWeight(3);
  stroke(150);
  noFill();
  getDisplayMachine().drawForSetup();
  stroke(255, 0, 0);
 
  for (Panel panel : getPanelsForTab(TAB_NAME_DETAILS))
  {
    panel.draw();
  }
  text(propertiesFilename, getPanel(PANEL_NAME_GENERAL).getOutline().getLeft(), getPanel(PANEL_NAME_GENERAL).getOutline().getTop()-7);

//  showCurrentMachinePosition();
  if (displayingInfoTextOnInputPage)
    showText(250,45);
  drawStatusText((int)statusTextPosition.x, (int)statusTextPosition.y);

  showCommandQueue((int) getDisplayMachine().getOutline().getRight()+6, 20);
}

public void drawRovingPage()
{
  strokeWeight(1);
  background(100);
  noFill();
  stroke(255, 150, 255, 100);
  strokeWeight(3);
  stroke(150);
  noFill();
  getDisplayMachine().drawForSetup();
  stroke(255, 0, 0);
 
  for (Panel panel : getPanelsForTab(TAB_NAME_ROVING))
  {
    panel.draw();
  }
  text(propertiesFilename, getPanel(PANEL_NAME_GENERAL).getOutline().getLeft(), getPanel(PANEL_NAME_GENERAL).getOutline().getTop()-7);

//  showCurrentMachinePosition();
  showGroupBox();
  showCurrentMachinePosition();
  if (displayingInfoTextOnInputPage)
    showText(250,45);
  drawStatusText((int)statusTextPosition.x, (int)statusTextPosition.y);

  showCommandQueue((int) getDisplayMachine().getOutline().getRight()+6, 20);
}

public void drawTracePage()
{
  strokeWeight(1);
  background(100);
  noFill();
  stroke(255, 150, 255, 100);
  strokeWeight(3);
  stroke(150);
  noFill();
  getDisplayMachine().drawForTrace();
  if (displayingImage && getDisplayMachine().imageIsReady() && retraceShape)
  {
    processedLiveImage = trace_processImageForTrace(getDisplayMachine().getImage());
    colourSeparations = trace_buildSeps(processedLiveImage, sepKeyColour);
    traceShape = trace_traceImage(colourSeparations);
    drawingTraceShape = true;
  }

  stroke(255, 0, 0);
 
  for (Panel panel : getPanelsForTab(TAB_NAME_TRACE))
  {
    panel.draw();
  }
  text(propertiesFilename, getPanel(PANEL_NAME_GENERAL).getOutline().getLeft(), getPanel(PANEL_NAME_GENERAL).getOutline().getTop()-7);


  if (displayingInfoTextOnInputPage)
    showText(250,45);
  drawStatusText((int)statusTextPosition.x, (int)statusTextPosition.y);
  showCommandQueue((int) width-200, 20);


//  processGamepadInput();
//
//  if (displayGamepadOverlay)
//    displayGamepadOverlay();
}


public void drawCommandQueuePage()
{
  cursor(ARROW);
  background(100);

  // machine outline
  fill(100);
  drawMachineOutline();
  showingSummaryOverlay = false;
  

  
  int right = 0;
  for (Panel panel : getPanelsForTab(TAB_NAME_QUEUE))
  {
    panel.draw();
    float r = panel.getOutline().getRight();
    if (r > right)
      right = (int) r;
  }
  text(propertiesFilename, getPanel(PANEL_NAME_GENERAL).getOutline().getLeft(), getPanel(PANEL_NAME_GENERAL).getOutline().getTop()-7);
  showCommandQueue(right, (int)mainPanelPosition.y);
  
  drawStatusText((int)statusTextPosition.x, (int)statusTextPosition.y);
  
}

public void drawImageLoadPage()
{
  drawImagePage();
}



public void drawMoveImageOutline()
{
  if (MODE_MOVE_IMAGE == currentMode && getDisplayMachine().getImage() != null)
  {
    // get scaled size of the  image
    PVector imageSize = getDisplayMachine().inMM(getDisplayMachine().getImageFrame().getSize());
    PVector imageSizeOnScreen = getDisplayMachine().scaleToScreen(imageSize);
    imageSizeOnScreen.sub(getDisplayMachine().getOutline().getTopLeft());
    PVector offset = new PVector(imageSizeOnScreen.x/2.0f, imageSizeOnScreen.y/2.0f);
    
    PVector mVect = getMouseVector();
    PVector imagePos = new PVector(mVect.x-offset.x, mVect.y-offset.y);

    fill(80,50);
    noStroke();
    rect(imagePos.x+imageSizeOnScreen.x, imagePos.y+4, 4, imageSizeOnScreen.y);
    rect(imagePos.x+4, imageSizeOnScreen.y+imagePos.y, imageSizeOnScreen.x-4, 4);
    tint(255,180);
    image(getDisplayMachine().getImage(), imagePos.x, imagePos.y, imageSizeOnScreen.x, imageSizeOnScreen.y);
    noTint();
    // decorate image
    noFill();
  }
  else if (MODE_MOVE_VECTOR == currentMode && getVectorShape() != null)
  {
    RPoint[][] pointPaths = getVectorShape().getPointsInPaths();
    RG.ignoreStyles();
    stroke(100);
    strokeWeight(1);

    // offset mouse vector so it grabs the centre of the shape
    PVector centroid = new PVector(getVectorShape().width/2, getVectorShape().height/2);
    centroid = PVector.mult(centroid, (vectorScaling/100));
    PVector offsetMouseVector = PVector.sub(getDisplayMachine().scaleToDisplayMachine(getMouseVector()), centroid);
    if (pointPaths != null)
    {
      for (int i = 0; i<pointPaths.length; i++)
      {
        if (pointPaths[i] != null) 
        {
          beginShape();
          for (int j = 0; j<pointPaths[i].length; j++)
          {
            PVector p = new PVector(pointPaths[i][j].x, pointPaths[i][j].y);
            p = PVector.mult(p, (vectorScaling/100));
            p = PVector.add(p, offsetMouseVector);
            p = getDisplayMachine().scaleToScreen(p);
            vertex(p.x, p.y);
          }
          endShape();
        }
      }
    }
  }
}

public void showCurrentMachinePosition()
{
  noStroke();
  fill(255,0,255,150);
  PVector pgCoord = getDisplayMachine().scaleToScreen(currentMachinePos);
  ellipse(pgCoord.x, pgCoord.y, 20, 20);

  // also show cartesian position if reported
  fill(255,255,0,150);
  ellipse(currentCartesianMachinePos.x, currentCartesianMachinePos.y, 15, 15);

  noFill();
}

public void showGroupBox()
{
  if (displayingGuides)
  {
    if (isBoxSpecified())
    {
      noFill();
      stroke(getFrameColour());
      strokeWeight(1);
      PVector topLeft = getDisplayMachine().scaleToScreen(boxVector1);
      PVector botRight = getDisplayMachine().scaleToScreen(boxVector2);
      rect(topLeft.x, topLeft.y, botRight.x-topLeft.x, botRight.y-topLeft.y);
    }
    else 
    {
      noFill();
      stroke(getFrameColour());
      strokeWeight(1);
  
      if (getBoxVector1() != null)
      {
        PVector topLeft = getDisplayMachine().scaleToScreen(boxVector1);
        line(topLeft.x, topLeft.y, topLeft.x-10, topLeft.y);
        line(topLeft.x, topLeft.y, topLeft.x, topLeft.y-10);
      }
  
      if (getBoxVector2() != null)
      {
        PVector botRight = getDisplayMachine().scaleToScreen(boxVector2);
        line(botRight.x, botRight.y, botRight.x+10, botRight.y);
        line(botRight.x, botRight.y, botRight.x, botRight.y+10);
      }
    }
  }
  
}

public void loadImageWithFileChooser()
{
  SwingUtilities.invokeLater(new Runnable() 
  {
    public void run() {
      JFileChooser fc = new JFileChooser();
      fc.setFileFilter(new ImageFileFilter());
      
      fc.setDialogTitle("Choose an image file...");

      int returned = fc.showOpenDialog(frame);
      if (returned == JFileChooser.APPROVE_OPTION) 
      {
        File file = fc.getSelectedFile();
        // see if it's an image
        PImage img = loadImage(file.getPath());
        if (img != null) 
        {
          img = null;
          getDisplayMachine().loadNewImageFromFilename(file.getPath());
          if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
          {
            getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
          }
        }
      }
    }
  });
}

class ImageFileFilter extends javax.swing.filechooser.FileFilter 
{
  public boolean accept(File file) {
      String filename = file.getName();
      filename.toLowerCase();
      if (file.isDirectory() || filename.endsWith(".png") || filename.endsWith(".jpg") || filename.endsWith(".jpeg")) 
        return true;
      else
        return false;
  }
  public String getDescription() {
      return "Image files (PNG or JPG)";
  }
}

public void loadVectorWithFileChooser()
{
  SwingUtilities.invokeLater(new Runnable() 
  {
    public void run() {
      JFileChooser fc = new JFileChooser();
      fc.setFileFilter(new VectorFileFilter());
      
      fc.setDialogTitle("Choose a vector file...");

      int returned = fc.showOpenDialog(frame);
      if (returned == JFileChooser.APPROVE_OPTION) 
      {
        File file = fc.getSelectedFile();
        if (file.exists())
        {
          RShape shape = RG.loadShape(file.getPath());
          if (shape != null) 
          {
            setVectorFilename(file.getPath());
            setVectorShape(shape);
          }
          else 
          {
            println("File not found (" + file.getPath() + ")");
          }
        }
      }
    }
  });
}
class VectorFileFilter extends javax.swing.filechooser.FileFilter 
{
  public boolean accept(File file) {
      String filename = file.getName();
      filename.toLowerCase();
      if (file.isDirectory() || filename.endsWith(".svg")) 
        return true;
      else
        return false;
  }
  public String getDescription() {
      return "Vector graphic files (SVG)";
  }
}

public void loadNewPropertiesFilenameWithFileChooser()
{
  SwingUtilities.invokeLater(new Runnable() 
  {
    public void run() 
    {
      JFileChooser fc = new JFileChooser();
      fc.setFileFilter(new PropertiesFileFilter());
      
      fc.setDialogTitle("Choose a config file...");

      int returned = fc.showOpenDialog(frame);
      if (returned == JFileChooser.APPROVE_OPTION) 
      {
        File file = fc.getSelectedFile();
        if (file.exists())
        {
          println("New properties file exists.");
          newPropertiesFilename = file.toString();
          println("new propertiesFilename: "+  newPropertiesFilename);
          propertiesFilename = newPropertiesFilename;
          // clear old properties.
          props = null;
          loadFromPropertiesFile();
          
          // set values of number spinners etc
          updateNumberboxValues();
        }   
      }
    }
  });
}

class PropertiesFileFilter extends javax.swing.filechooser.FileFilter 
{
  public boolean accept(File file) {
      String filename = file.getName();
      filename.toLowerCase();
      if (file.isDirectory() || filename.endsWith(".properties.txt")) 
        return true;
      else
        return false;
  }
  public String getDescription() {
      return "Properties files (*.properties.txt)";
  }
}

public void saveNewPropertiesFileWithFileChooser()
{
  SwingUtilities.invokeLater(new Runnable() 
  {
    public void run() 
    {
      JFileChooser fc = new JFileChooser();
      fc.setFileFilter(new PropertiesFileFilter());
      
      fc.setDialogTitle("Enter a config file name...");

      int returned = fc.showSaveDialog(frame);
      if (returned == JFileChooser.APPROVE_OPTION) 
      {
        File file = fc.getSelectedFile();
        newPropertiesFilename = file.toString();
        newPropertiesFilename.toLowerCase();
        if (!newPropertiesFilename.endsWith(".properties.txt"))
          newPropertiesFilename+=".properties.txt";
          
        println("new propertiesFilename: "+  newPropertiesFilename);
        propertiesFilename = newPropertiesFilename;
        savePropertiesFile();
        // clear old properties.
        props = null;
        loadFromPropertiesFile();
      }
    }
  });
}



public void setPictureFrameDimensionsToBox()
{
//  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
//  {
    Rectangle r = new Rectangle(getDisplayMachine().inSteps(getBoxVector1()), getDisplayMachine().inSteps(getBoxVectorSize()));
    getDisplayMachine().setPictureFrame(r);
//  }
}
public void setBoxToPictureframeDimensions()
{
  setBoxVector1(getDisplayMachine().inMM(getDisplayMachine().getPictureFrame().getTopLeft()));
  setBoxVector2(getDisplayMachine().inMM(getDisplayMachine().getPictureFrame().getBotRight()));
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    Toggle t = (Toggle) getAllControls().get(MODE_SHOW_IMAGE);
    t.setValue(0);
    t.update();

    t = (Toggle) getAllControls().get(MODE_SHOW_DENSITY_PREVIEW);
    t.setValue(1);
    t.update();
    
  }
}

public float getGridSize()
{
  return this.gridSize;
}
public void setGridSize(float s)
{
  this.gridSize = s;
}

public void setSampleArea(float v)
{
  this.sampleArea = v;
}

public void controlEvent(ControlEvent controlEvent) 
{
  if (controlEvent.isTab()) 
  {
    if (controlEvent.tab().name() == getCurrentTab())
    {
      // already here.
      println("Already here.");
    }
    else
    {
      changeTab(currentTab, controlEvent.tab().name());
    }
  }
  else if(controlEvent.isGroup()) 
  {
    print("got an event from "+controlEvent.group().name()+"\t");

    // checkbox uses arrayValue to store the state of 
    // individual checkbox-items. usage:
    for (int i=0; i<controlEvent.group().arrayValue().length; i++) 
    {
      int n = (int)controlEvent.group().arrayValue()[i];
    }
    println();
  }
  
}

public void changeTab(String from, String to)
{
  
  // hide old panels
  currentTab = to;
  for (Panel panel : getPanelsForTab(currentTab))
  {
    for (Controller c : panel.getControls())
    {
      c.moveTo(currentTab);
      c.show();
    }
  }
  
}



public boolean mouseOverMachine()
{
  boolean result = false;
  if (isMachineClickable())
  {
    if (getDisplayMachine().getOutline().surrounds(getMouseVector())
      && mouseOverControls().isEmpty())
    {
      result = true;
    }
    else
      result = false;
  }
  return result;
}

public Set<Controller> mouseOverControls()
{
  Set<Controller> set = new HashSet<Controller>(1);
  for (String key : getAllControls().keySet())
  {
    if (getAllControls().get(key).isInside())
    {
      set.add(getAllControls().get(key));
    }
  }
  return set;
}


public boolean isMachineClickable()
{
  if (getCurrentTab() == TAB_NAME_INPUT)
  {
    return true;
  }
  else if (getCurrentTab() == TAB_NAME_ROVING)
  {
    return true;
  }
  else if (getCurrentTab() == TAB_NAME_QUEUE)
  {
    return false;
  }
  else if (getCurrentTab() == TAB_NAME_DETAILS)
  {
    return false;
  }
  else
  {
    return false;
  }
}
public boolean isPanelClickable()
{
  return true;
}
public boolean isQueueClickable()
{
  return true;
}

public boolean mouseOverPanel()
{
  boolean result = false;
  for (Panel panel : getPanelsForTab(currentTab))
  {
    if (panel.getOutline().surrounds(getMouseVector()))
      result = true;
  }
  return result;
}

public boolean mouseOverQueue()
{
  boolean result = true;
  if (mouseX < leftEdgeOfQueue
    || mouseX > rightEdgeOfQueue
    || mouseY < topEdgeOfQueue
    || mouseY > bottomEdgeOfQueue)
    result = false;
  return result;
}

public void changeMachineScaling(int delta)
{
  boolean scalingChanged = true;
  machineScaling += (delta * 0.1f);
  if (machineScaling <  MIN_SCALING)
  {
    machineScaling = MIN_SCALING;
    scalingChanged = false;
  }
  else if (machineScaling > MAX_SCALING)
  {
    machineScaling = MAX_SCALING;
    scalingChanged = false;
  }
}

public boolean checkKey(int k)
{
  if (keys.length >= k) {
    return keys[k];  
  }
  return false;
}

public void keyReleased()
{ 
  keys[keyCode] = false; 
}

public void keyPressed()
{

  keys[keyCode] = true;
  println(KeyEvent.getKeyText(keyCode));
  
  if (checkKey(CONTROL) && checkKey(KeyEvent.VK_PAGE_UP)) 
    changeMachineScaling(1);
  else if (checkKey(CONTROL) && checkKey(KeyEvent.VK_PAGE_DOWN)) 
    changeMachineScaling(-1);
  else if (checkKey(CONTROL) && checkKey(DOWN))
    getDisplayMachine().getOffset().y = getDisplayMachine().getOffset().y + 10;
  else if (checkKey(CONTROL) && checkKey(UP)) 
    getDisplayMachine().getOffset().y = getDisplayMachine().getOffset().y - 10;
  else if (checkKey(CONTROL) && checkKey(RIGHT)) 
    getDisplayMachine().getOffset().x = getDisplayMachine().getOffset().x + 10;
  else if (checkKey(CONTROL) && checkKey(LEFT)) 
    getDisplayMachine().getOffset().x = getDisplayMachine().getOffset().x - 10;
  else if (checkKey(KeyEvent.VK_ESCAPE))
    key = 0;

//  if (checkKey(CONTROL) && checkKey(KeyEvent.VK_G)) 
//    println("CTRL+G");

  else if (checkKey(CONTROL) && checkKey(KeyEvent.VK_G))
  {
    Toggle t = (Toggle) getAllControls().get(MODE_SHOW_GUIDES);
    if (displayingGuides)
    {
      minitoggle_mode_showGuides(false);
      t.setValue(0);
    }
    else
    {
      minitoggle_mode_showGuides(true);
      t.setValue(1);
    }
    t.update();
  }
  else if (checkKey(CONTROL) && checkKey(KeyEvent.VK_C))
  {
    if (isUseWindowedConsole())
      setUseWindowedConsole(false);
    else
      setUseWindowedConsole(true);
      
    initLogging();
  }
  else if (checkKey(CONTROL) && checkKey(KeyEvent.VK_S))
  {
    if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
      displayingSelectedCentres = (displayingSelectedCentres) ? false : true;
  }
  else if (checkKey(CONTROL) && checkKey(KeyEvent.VK_I))
  {
    displayingInfoTextOnInputPage = (displayingInfoTextOnInputPage) ? false : true;
  }
//  else if (key == '+')
//  {
//    currentMachineMaxSpeed = currentMachineMaxSpeed+MACHINE_MAXSPEED_INCREMENT;
//    currentMachineMaxSpeed =  Math.round(currentMachineMaxSpeed*100.0)/100.0;
//    NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
//    DecimalFormat df = (DecimalFormat)nf;  
//    df.applyPattern("###.##");
//    addToRealtimeCommandQueue(CMD_SETMOTORSPEED+df.format(currentMachineMaxSpeed)+",END");
//  }
//  else if (key == '-')
//  {
//    currentMachineMaxSpeed = currentMachineMaxSpeed+(0.0 - MACHINE_MAXSPEED_INCREMENT);
//    currentMachineMaxSpeed =  Math.round(currentMachineMaxSpeed*100.0)/100.0;
//    NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
//    DecimalFormat df = (DecimalFormat)nf;  
//    df.applyPattern("###.##");
//    addToRealtimeCommandQueue(CMD_SETMOTORSPEED+df.format(currentMachineMaxSpeed)+",END");
//  }
//  else if (key == '*')
//  {
//    currentMachineAccel = currentMachineAccel+MACHINE_ACCEL_INCREMENT;
//    currentMachineAccel =  Math.round(currentMachineAccel*100.0)/100.0;
//    NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
//    DecimalFormat df = (DecimalFormat)nf;  
//    df.applyPattern("###.##");
//    addToRealtimeCommandQueue(CMD_SETMOTORACCEL+df.format(currentMachineAccel)+",END");
//  }
//  else if (key == '/')
//  {
//    currentMachineAccel = currentMachineAccel+(0.0 - MACHINE_ACCEL_INCREMENT);
//    currentMachineAccel =  Math.round(currentMachineAccel*100.0)/100.0;
//    NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
//    DecimalFormat df = (DecimalFormat)nf;  
//    df.applyPattern("###.##");
//    addToRealtimeCommandQueue(CMD_SETMOTORACCEL+df.format(currentMachineAccel)+",END");
//  }
//  else if (key == ']')
//  {
//    currentPenWidth = currentPenWidth+penIncrement;
//    currentPenWidth =  Math.round(currentPenWidth*100.0)/100.0;
//    NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
//    DecimalFormat df = (DecimalFormat)nf;  
//    df.applyPattern("###.##");
//    addToRealtimeCommandQueue(CMD_CHANGEPENWIDTH+df.format(currentPenWidth)+",END");
//  }
//  else if (key == '[')
//  {
//    currentPenWidth = currentPenWidth-penIncrement;
//    currentPenWidth =  Math.round(currentPenWidth*100.0)/100.0;
//    NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
//    DecimalFormat df = (DecimalFormat)nf;  
//    df.applyPattern("###.##");
//    addToRealtimeCommandQueue(CMD_CHANGEPENWIDTH+df.format(currentPenWidth)+",END");
//  }
  else if (key == '#' )
  {
    addToRealtimeCommandQueue(CMD_PENUP+"END");
  }
  else if (key == '~')
  {
    addToRealtimeCommandQueue(CMD_PENDOWN+"END");
  }
  else if (key == '<')
  {
    if (this.maxSegmentLength > 1)
      this.maxSegmentLength--;
  }
  else if (key == '>')
  {
    this.maxSegmentLength++;
  }
//  else if (key == ',')
//  {
//    if (this.minimumVectorLineLength > 0)
//      this.minimumVectorLineLength--;
//  }
//  else if (key == '.')
//  {
//    this.minimumVectorLineLength++;
//  }
}
public void mouseDragged()
{
  if (mouseOverControls().isEmpty())
  {
    if (mouseButton == CENTER)
    {
      machineDragged();
    }
    else if (mouseButton == LEFT)
    {
      if (currentMode.equals(MODE_INPUT_BOX_TOP_LEFT))
      {
        // dragging a selection area
        PVector pos = getDisplayMachine().scaleToDisplayMachine(getMouseVector());
        setBoxVector2(pos);
      }
    }
  }
}
  
public void mouseClicked()
{
  if (mouseOverPanel())
  { // changing mode
//    panelClicked();
  }
  else
  {
    if (currentMode.equals(MODE_MOVE_IMAGE))
    {
      PVector imageSize = getDisplayMachine().inMM(getDisplayMachine().getImageFrame().getSize());
      PVector mVect = getDisplayMachine().scaleToDisplayMachine(getMouseVector());
      PVector offset = new PVector(imageSize.x/2.0f, imageSize.y/2.0f);
      PVector imagePos = new PVector(mVect.x-offset.x, mVect.y-offset.y);
  
      imagePos = getDisplayMachine().inSteps(imagePos);
      getDisplayMachine().getImageFrame().setPosition(imagePos.x, imagePos.y);
  
      if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
        getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    }
    else if (currentMode.equals(MODE_MOVE_VECTOR))
    {
      // offset mouse vector so it grabs the centre of the shape
      PVector centroid = new PVector(getVectorShape().width/2, getVectorShape().height/2);
      centroid = PVector.mult(centroid, (vectorScaling/100));
      PVector offsetMouseVector = PVector.sub(getDisplayMachine().scaleToDisplayMachine(getMouseVector()), centroid);
      vectorPosition = offsetMouseVector;
    }
    else if (mouseOverQueue())
    {
      // stopping or starting 
      println("queue clicked.");
      queueClicked();
    }
    else if (mouseOverMachine())
    { 
      // picking coords
      machineClicked();
    }
  }
}

public void machineDragged()
{
  if (mouseButton == CENTER)
  {
    PVector currentPos = getMouseVector();
    PVector change = PVector.sub(currentPos, lastMachineDragPosition);
    lastMachineDragPosition = new PVector(currentPos.x, currentPos.y);
    PVector currentPosition = getDisplayMachine().getOutline().getPosition();
    getDisplayMachine().getOffset().add(change);
  }
}

public void machineClicked()
{
  if (mouseButton == LEFT)
  {
    leftButtonMachineClick();
  }
}
public void mousePressed()
{
//  println("mouse pressed");
//  println("mouse button: "+mouseButton);
//  println("Current mode: " +currentMode);
  if (mouseButton == CENTER)
  {
    middleButtonMachinePress();
    lastMachineDragPosition = getMouseVector();
  }
  else if (mouseButton == LEFT)
  {
    if (MODE_INPUT_BOX_TOP_LEFT.equals(currentMode) && mouseOverMachine())
    {
      minitoggle_mode_showImage(true);
      minitoggle_mode_showDensityPreview(false);
      PVector pos = getDisplayMachine().scaleToDisplayMachine(getMouseVector());
      setBoxVector1(pos);
      if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
      {
        getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
//        minitoggle_mode_showImage(false);
//        minitoggle_mode_showDensityPreview(true);
      }
    }
    else
    {
//      println("Do nothing.");
    }
  }
}

public void mouseReleased()
{
  if (mouseButton == LEFT)
  {
    if (MODE_INPUT_BOX_TOP_LEFT.equals(currentMode) && mouseOverMachine())
    {
      PVector pos = getDisplayMachine().scaleToDisplayMachine(getMouseVector());
      setBoxVector2(pos);
      if (isBoxSpecified())
      {
        if (getBoxVector1().x > getBoxVector2().x)
        {
          float temp = getBoxVector1().x;
          getBoxVector1().x = getBoxVector2().x;
          getBoxVector2().x = temp;
        }
        if (getBoxVector1().y > getBoxVector2().y)
        {
          float temp = getBoxVector1().y;
          getBoxVector1().y = getBoxVector2().y;
          getBoxVector2().y = temp;
        }
        if (getDisplayMachine().pixelsCanBeExtracted())
        {
          getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
          minitoggle_mode_showImage(false);
          minitoggle_mode_showDensityPreview(true);
          getAllControls().get(MODE_SHOW_IMAGE).setValue(0);
          getAllControls().get(MODE_SHOW_DENSITY_PREVIEW).setValue(1);
        }
      }
    }
  }
}

public void middleButtonMachinePress()
{
  PVector machineDragOffset = PVector.sub(getMouseVector(), getDisplayMachine().getOutline().getPosition());
  this.machineDragOffset = machineDragOffset;
}

public void leftButtonMachineClick()
{
  if (currentMode.equals(MODE_BEGIN))
    currentMode = MODE_INPUT_BOX_TOP_LEFT;
  else if (currentMode.equals(MODE_SET_POSITION))
    sendSetPosition();
  else if (currentMode.equals(MODE_DRAW_DIRECT))
    sendMoveToPosition(true);
  else if (currentMode.equals(MODE_DRAW_TO_POSITION))
    sendMoveToPosition(false);
  else if (currentMode.equals(MODE_CHOOSE_CHROMA_KEY_COLOUR))
    setChromaKey(getMouseVector());
  else if (currentMode.equals(MODE_SEND_START_TEXT))
    sendStartTextAtPoint();
  
}

public void mouseWheel(int delta) 
{
  changeMachineScaling(delta);
} 

public void setChromaKey(PVector p)
{
  int col = getDisplayMachine().getPixelAtScreenCoords(p);
  chromaKeyColour = col;
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
  }
}

public boolean isPreviewable(String command)
{
  if (command.startsWith(CMD_CHANGELENGTHDIRECT) 
    || command.startsWith(CMD_CHANGELENGTH)
    || command.startsWith(CMD_DRAWPIXEL))
  {
    return true;
  }
  else
  {
    return false;
  }
}

/**
  This will comb the command queue and attempt to draw a picture of what it contains.
  Coordinates here are in pixels.
*/
public void previewQueue()
{
  PVector startPoint = null;
  
  if (commandQueue.hashCode() != lastCommandQueueHash)
  {
    println("regenerating preview queue.");
    previewCommandList.clear();
    for (String command : commandQueue)
    {
      if (command.startsWith(CMD_CHANGELENGTHDIRECT) || command.startsWith(CMD_CHANGELENGTH) || command.startsWith(CMD_DRAWPIXEL))
      {
        String[] splitted = split(command, ",");

        PreviewVector pv = new PreviewVector();
        pv.command = splitted[0];

        String aLenStr = splitted[1];
        String bLenStr = splitted[2];
        
        PVector endPoint = new PVector(Integer.parseInt(aLenStr), Integer.parseInt(bLenStr));
        endPoint = getDisplayMachine().asCartesianCoords(endPoint);
        endPoint = getDisplayMachine().inMM(endPoint);
        
        pv.x = endPoint.x;
        pv.y = endPoint.y;
        pv.z = -1.0f;
        
        if (command.startsWith(CMD_DRAWPIXEL))
        {
          String densStr = splitted[4];
          pv.z = Integer.parseInt(densStr);
        }
        
        previewCommandList.add(pv);
      }
    }
    lastCommandQueueHash = commandQueue.hashCode();
  }
  
  for (PreviewVector pv : previewCommandList)
  {
    PVector p = (PVector) pv;
    p = getDisplayMachine().scaleToScreen(p);

    if (startPoint == null)
    {
      noStroke();
      fill(255,0,255,150);
      startPoint = getDisplayMachine().scaleToScreen(currentMachinePos);
      ellipse(p.x, p.y, 20, 20);
      noFill();
    }
    
    if (pv.command.equals(CMD_CHANGELENGTHDIRECT))
      stroke(0);
    else 
      stroke(200,0,0);
    line(startPoint.x, startPoint.y, p.x, p.y);
    startPoint = p;

    if (pv.z >= 0.0f)
    {
      noStroke();
      fill(255,pv.z,pv.z);
      ellipse(p.x, p.y, 5,5);
      noFill();
    }

  }

  if (startPoint != null)
  {
    noStroke();
    fill(200,0,0,128);
    ellipse(startPoint.x, startPoint.y, 15,15);
    noFill();
  }
  
}

public boolean isHiddenPixel(PVector p)
{
  if ((p.z == MASKED_PIXEL_BRIGHTNESS) || (p.z > pixelExtractBrightThreshold) || (p.z < pixelExtractDarkThreshold))
    return true;
  else
    return false;
}
  


public void sizeImageToFitBox()
{
//  PVector mmBoxSize = getDisplayMachine().inSteps(getBoxSize());
//  PVector mmBoxPos = getDisplayMachine().inSteps(getBoxVector1());
//  println("mm box: " + mmBoxSize);
  
  PVector boxSize = getDisplayMachine().inSteps(getBoxSize());
  PVector boxPos = getDisplayMachine().inSteps(getBoxVector1());
  println("image: " + boxSize);
  
  Rectangle r = new Rectangle(boxPos, boxSize);
  getDisplayMachine().setImageFrame(r);
}

public void exportQueueToFile()
{
  if (!commandQueue.isEmpty() || !realtimeCommandQueue.isEmpty())
  {
    String savePath = selectOutput();  // Opens file chooser
    if (savePath == null) 
    {
      // If a file was not selected
      println("No output file was selected...");
    } 
    else 
    {
      // If a file was selected, print path to folder
      println("Output file: " + savePath);
      List<String> allCommands = new ArrayList<String>(realtimeCommandQueue);
      allCommands.addAll(commandQueue);
      
      String[] list = (String[]) allCommands.toArray(new String[0]);
      saveStrings(savePath, list);
      println("Completed queue export, " + list.length + " commands exported.");
    }  
  }
}
public void importQueueFromFile()
{
  commandQueue.clear();
  String loadPath = selectInput();
  if (loadPath == null)
  {
    // nothing selected
    println("No input file was selected.");
  }
  else
  {
    println("Input file: " + loadPath);
    String commands[] = loadStrings(loadPath);
//    List<String> list = Arrays
    commandQueue.addAll(Arrays.asList(commands));
    println("Completed queue import, " + commandQueue.size() + " commands found.");
  }
}

public String importTextToWriteFromFile()
{
  String loadPath = selectInput();
  String result = "";
  if (loadPath == null)
  {
    // nothing selected
    println("No input file was selected.");
  }
  else
  {
    println("Input file: " + loadPath);
    List<String> rows = java.util.Arrays.asList(loadStrings(loadPath));
    StringBuilder sb = new StringBuilder(200);
    for (String row : rows) 
    {
      sb.append(row);
    }
    result = sb.toString();

    println("Completed text import, " + result.length() + " characters found.");
  }
  return result;
}



public void queueClicked()
{
  int relativeCoord = (mouseY-topEdgeOfQueue);
  int rowClicked = relativeCoord / queueRowHeight;
  int totalCommands = commandQueue.size()+realtimeCommandQueue.size();
  
  if (rowClicked < 1) // its the header - start or stop queue
  {
    if (commandQueueRunning)
      commandQueueRunning = false;
    else
      commandQueueRunning = true;
  }
  else if (rowClicked > 2 && rowClicked < totalCommands+3) // it's a command from the queue
  {
    int cmdNumber = rowClicked-2;
    if (commandQueueRunning)
    {
      // if its running, then clicking on a command will mark it as a pause point
    }
    else
    {
      // if it's not running, then clicking on a command row will remove it
      if (!realtimeCommandQueue.isEmpty())
      {
        if (cmdNumber <= realtimeCommandQueue.size())
          realtimeCommandQueue.remove(cmdNumber-1);
        else  
        {
          cmdNumber-=(realtimeCommandQueue.size()+1);
          commandQueue.remove(cmdNumber);
        }        
      }
      else
      {
        commandQueue.remove(cmdNumber-1);
      }
    }
  }
}


public boolean isRowsSpecified()
{
  if (rowsVector1 != null && rowsVector2 != null)
    return true;
  else
    return false;
}

public boolean isBoxSpecified()
{
  if (boxVector1 != null && boxVector2 != null)
  {
    return true;
  }
  else
    return false;
}

public void setBoxVector1(PVector vec)
{
  boxVector1 = vec;
}
public void setBoxVector2(PVector vec)
{
  boxVector2 = vec;
}
public PVector getBoxVector1()
{
  return this.boxVector1;
}
public PVector getBoxVector2()
{
  return this.boxVector2;
}
public PVector getBoxVectorSize()
{
  return PVector.sub(getBoxVector2(),getBoxVector1());
}

public float getSampleArea()
{
  return this.sampleArea;
}


public void resetQueue()
{
  currentMode = MODE_BEGIN;
  commandQueue.clear();
  realtimeCommandQueue.clear();
}

public void showText(int xPosOrigin, int yPosOrigin)
{
  noStroke();
  fill(0, 0, 0, 80);
  rect(xPosOrigin, yPosOrigin, 220, 550);
  
  
  textSize(12);
  fill(255);
  int tRow = 15;
  int textPositionX = xPosOrigin+4;
  int textPositionY = yPosOrigin+4;
  
  int tRowNo = 1;
  PVector screenCoordsCart = getMouseVector();
 
  text(programTitle, textPositionX, textPositionY+(tRow*tRowNo++));
  tRowNo++;
  text("Cursor position: " + mouseX + ", " + mouseY, textPositionX, textPositionY+(tRow*tRowNo++));
  
  text("MM Per Step: " + getDisplayMachine().getMMPerStep(), textPositionX, textPositionY+(tRow*tRowNo++));
  text("Steps Per MM: " + getDisplayMachine().getStepsPerMM() ,textPositionX, textPositionY+(tRow*tRowNo++));

  if (getDisplayMachine().getOutline().surrounds(screenCoordsCart))
  {
    PVector posOnMachineCartesianInMM = getDisplayMachine().scaleToDisplayMachine(screenCoordsCart);
    text("Machine x/y mm: " + posOnMachineCartesianInMM.x+","+posOnMachineCartesianInMM.y, textPositionX, textPositionY+(tRow*tRowNo++));
    
    PVector posOnMachineNativeInMM = getDisplayMachine().convertToNative(posOnMachineCartesianInMM);
    text("Machine a/b mm: " + posOnMachineNativeInMM.x+","+posOnMachineNativeInMM.y, textPositionX, textPositionY+(tRow*tRowNo++));
  
    PVector posOnMachineNativeInSteps = getDisplayMachine().inSteps(posOnMachineNativeInMM);
    text("Machine a/b steps: " + posOnMachineNativeInSteps.x+","+posOnMachineNativeInSteps.y, textPositionX, textPositionY+(tRow*tRowNo++));
  }
  else
  {
    text("Machine x/y mm: --,--", textPositionX, textPositionY+(tRow*tRowNo++));
    text("Machine a/b mm: --,--", textPositionX, textPositionY+(tRow*tRowNo++));
    text("Machine a/b steps: --,--", textPositionX, textPositionY+(tRow*tRowNo++));
  }
  


  drawStatusText(textPositionX, textPositionY+(tRow*tRowNo++));  
    
  text(commandStatus, textPositionX, textPositionY+(tRow*tRowNo++));
  
  text("Mode: " + currentMode, textPositionX, textPositionY+(tRow*tRowNo++));

  // middle side
  text("Grid size: " + getGridSize(), textPositionX, textPositionY+(tRow*tRowNo++));
  
  text("Box width: " + getBoxWidth(), textPositionX, textPositionY+(tRow*tRowNo++));
  text("Box height: " + getBoxHeight(), textPositionX, textPositionY+(tRow*tRowNo++));

  text("Box offset left: " + getBoxPosition().x, textPositionX, textPositionY+(tRow*tRowNo++));
  text("Box offset top: " + getBoxPosition().y, textPositionX, textPositionY+(tRow*tRowNo++));
  
  text("Available memory: " + machineAvailMem + " (min: " + machineMinAvailMem +", used: "+ machineUsedMem+")", textPositionX, textPositionY+(tRow*tRowNo++));

  text("Time cmd: " + getCurrentPixelTime() + ", total: " + getTimeSoFar(), textPositionX, textPositionY+(tRow*tRowNo++));
  text("Average time per cmd: " + getAveragePixelTime(), textPositionX, textPositionY+(tRow*tRowNo++));
  text("Time to go: " + getTimeRemainingMins() + " mins (" + getTimeRemainingSecs() + " secs)", textPositionX, textPositionY+(tRow*tRowNo++));

  text("Commands sent: " + getPixelsCompleted() + ", remaining: " + getPixelsRemaining(), textPositionX, textPositionY+(tRow*tRowNo++));

  text("Estimated complete: " + getEstimatedCompletionTime(), textPositionX, textPositionY+(tRow*tRowNo++));

  text("Pixel sample area: " + sampleArea, textPositionX, textPositionY+(tRow*tRowNo++));
  text("Pixel drawing scale: " + getPixelScalingOverGridSize(), textPositionX, textPositionY+(tRow*tRowNo++));
  text("Max line segment length: " + getMaxSegmentLength(), textPositionX, textPositionY+(tRow*tRowNo++));
  text("Ignore vector lines shorter than: " + minimumVectorLineLength, textPositionX, textPositionY+(tRow*tRowNo++));
  text("Zoom: " + machineScaling, textPositionX, textPositionY+(tRow*tRowNo++));

  tRowNo++;
  text("Machine settings:", textPositionX, textPositionY+(tRow*tRowNo++));
  text("Last sent pen width: " + currentPenWidth, textPositionX, textPositionY+(tRow*tRowNo++));
  text("Last sent speed: " + currentMachineMaxSpeed, textPositionX, textPositionY+(tRow*tRowNo++));
  text("Last sent accel: " + currentMachineAccel, textPositionX, textPositionY+(tRow*tRowNo++));

  tRowNo++;
  text("Chroma key colour: ", textPositionX, textPositionY+(tRow*tRowNo));
  fill(chromaKeyColour);
  stroke(255);
  strokeWeight(1);
  rect(textPositionX+120, textPositionY+(tRow*tRowNo)-15, 25, 15);
  noFill();
  noStroke();
  tRowNo++;

}

public void drawStatusText(int x, int y)
{
  String drawbotStatus = null;
  
  if (useSerialPortConnection)
  {
    if (isDrawbotConnected())
    {
      if (drawbotReady)
      {
        fill(0, 200, 0);
        if (currentHardware >= HARDWARE_VER_MEGA_POLARSHIELD)
          drawbotStatus = "Polargraph READY! (PolargraphSD)";
        else if (currentHardware >= HARDWARE_VER_MEGA)
          drawbotStatus = "Polargraph READY! (Mega)";
        else
          drawbotStatus = "Polargraph READY! (Uno)";
      }
      else
      {
        fill(200, 200, 0);
        String busyDoing = lastCommand;
        if ("".equals(busyDoing))
          busyDoing = commandHistory.get(commandHistory.size()-1);
        drawbotStatus = "BUSY: " + busyDoing;
      }  
    }
    else
    {
      fill(255, 0, 0);
      drawbotStatus = "Polargraph is not connected.";
    }  
  }
  else
  {
    fill(255, 0, 0);
    drawbotStatus = "No serial connection.";
  }
  
  text(drawbotStatus, x, y);
  fill(255);
}

public void setCommandQueueFont()
{
  textSize(12);
  fill(255);
}  
public void showCommandQueue(int xPos, int yPos)
{
  setCommandQueueFont();
  int tRow = 15;
  int textPositionX = xPos;
  int textPositionY = yPos;
  int tRowNo = 1;

  int commandQueuePos = textPositionY+(tRow*tRowNo++);

  topEdgeOfQueue = commandQueuePos-queueRowHeight;
  leftEdgeOfQueue = textPositionX;
  rightEdgeOfQueue = textPositionX+300;
  bottomEdgeOfQueue = height;
  
  drawCommandQueueStatus(textPositionX, commandQueuePos, 14);
  commandQueuePos+=queueRowHeight;
  text("Last command: " + ((commandHistory.isEmpty()) ? "-" : commandHistory.get(commandHistory.size()-1)), textPositionX, commandQueuePos);
  commandQueuePos+=queueRowHeight;
  text("Current command: " + lastCommand, textPositionX, commandQueuePos);
  commandQueuePos+=queueRowHeight;
  
  fill(128,255,255);
  int queueNumber = commandQueue.size()+realtimeCommandQueue.size();
  for (String s : realtimeCommandQueue)
  {
    text((queueNumber--)+". "+ s, textPositionX, commandQueuePos);
    commandQueuePos+=queueRowHeight;
  }
  
  fill(255);
  try
  {
    // Write out the commands into the window, stop when you fall off the bottom of the window
    // Or run out of commands
    int commandNo = 0;
    while (commandQueuePos <= height && commandNo < commandQueue.size())
    {
      String s = commandQueue.get(commandNo);
      text((queueNumber--)+". "+ s, textPositionX, commandQueuePos);
      commandQueuePos+=queueRowHeight;
      commandNo++;
    }
  }
  catch (ConcurrentModificationException cme)
  {
    // not doing anything with this exception - I don't mind if it's wrong on the screen for a second or two.
    println("Caught the pesky ConcurrentModificationException: " + cme.getMessage());
  }
}

public void drawCommandQueueStatus(int x, int y, int tSize)
{
  String queueStatus = null;
  textSize(tSize);
  if (commandQueueRunning)
  {
    queueStatus = "QUEUE RUNNING - click to pause";
    fill(0, 200, 0);
  }
  else
  {
    queueStatus = "QUEUE PAUSED - click to start";
    fill(255, 0, 0);
  }

  text("CommandQueue: " + queueStatus, x, y);
  setCommandQueueFont();
}

public long getCurrentPixelTime()
{
  if (pixelTimerRunning)
    return new Date().getTime() - timeLastPixelStarted.getTime();
  else
    return 0L;
}
public long getAveragePixelTime()
{
  if (pixelTimerRunning)
  {
    long msElapsed = timeLastPixelStarted.getTime() - timerStart.getTime();
    int pixelsCompleted = getPixelsCompleted();
    if (pixelsCompleted > 0)
      return msElapsed / pixelsCompleted;
    else
      return 0L;
  }
  else
    return 0L;
}
public long getTimeSoFar()
{
  if (pixelTimerRunning)
    return new Date().getTime() - timerStart.getTime();
  else
    return 0L;
}
public long getTimeRemaining()
{
  if (pixelTimerRunning)
    return getTotalEstimatedTime() - getTimeSoFar();
  else
    return 0L;
}
public long getTotalEstimatedTime()
{
  if (pixelTimerRunning)
    return (getAveragePixelTime() * numberOfPixelsTotal);
  else
    return 0L;
}
public long getTimeRemainingSecs()
{
  if (pixelTimerRunning)
    return getTimeRemaining() / 1000L;
  else
    return 0L;
}
public long getTimeRemainingMins()
{
  if (pixelTimerRunning)
    return getTimeRemainingSecs()/60L;
  else
    return 0L;
}
public String getEstimatedCompletionTime()
{
  if (pixelTimerRunning)
  {
    long totalTime = getTotalEstimatedTime()+timerStart.getTime();
    return sdf.format(totalTime);
  }
  else
    return "TIMER NOT RUNNING";
}

public int getPixelsCompleted()
{
  if (pixelTimerRunning)
    return numberOfPixelsCompleted-1;
  else
    return 0;
}
public int getPixelsRemaining()
{
  if (pixelTimerRunning)
    return numberOfPixelsTotal - getPixelsCompleted();
  else
    return 0;
}


public float getBoxWidth()
{
  if (boxVector1 != null && boxVector2 != null)
    return (boxVector2.x-boxVector1.x);
  else
    return 0;
}

public float getBoxHeight()
{
  if (boxVector1 != null && boxVector2 != null)
    return (boxVector2.y-boxVector1.y);
  else
    return 0;
}
public PVector getBoxSize()
{
  PVector p = PVector.sub(getBoxVector2(), getBoxVector1());
  return p;
}

public PVector getBoxPosition()
{
  if (boxVector1 != null)
    return boxVector1;
  else
    return new PVector();
}

public void clearBoxVectors()
{
  setBoxVector1(null);
  setBoxVector2(null);
  getDisplayMachine().setExtractedPixels(null);
}

public PVector getHomePoint()
{
  return this.homePointCartesian;
}



//public Machine getMachine()
//{
//  return this.machine;
//}
public DisplayMachine getDisplayMachine()
{
  if (displayMachine == null)
    displayMachine = new DisplayMachine(new Machine(5000, 5000, 800.0f, 95.0f), machinePosition, machineScaling);
    
  displayMachine.setOffset(machinePosition);
  displayMachine.setScale(machineScaling);
  return displayMachine;
}

public Integer getHardwareVersion()
{
  return this.currentHardware;
}

public void changeHardwareVersionTo(int newVer)
{
  this.currentHardware = newVer;

  this.panelNames = null;
  this.tabNames = null;
  this.controlNames = null;
  this.controlsForPanels = null;

  this.panelsForTabs = null;
  this.panels = null;

  switch (newVer)
  {
    case HARDWARE_VER_MEGA :
      currentSram = HARDWARE_ATMEGA1280_SRAM;
    default   :  
      currentSram = HARDWARE_ATMEGA328_SRAM;
  }
//  windowResized();
}

public void setHardwareVersionFromIncoming(String readyString)
{
  int newHardwareVersion = HARDWARE_VER_UNO;
  if ("READY".equals(readyString))
  {
    newHardwareVersion = HARDWARE_VER_UNO;
  }
  else
  {
    String ver = readyString.substring(6);
    int verInt = HARDWARE_VER_UNO;
    try
    {
      verInt = Integer.parseInt(ver);
    }
    catch (NumberFormatException nfe)
    {
      println("Bad format for hardware version - defaulting to ATMEGA328 (Uno)");
      verInt = HARDWARE_VER_UNO;
    }
    
    if (HARDWARE_VER_MEGA == verInt 
    || HARDWARE_VER_MEGA_POLARSHIELD == verInt)
      newHardwareVersion = verInt;
    else
      newHardwareVersion = HARDWARE_VER_UNO;
  }
  
  // now see if it's different to last time.
  if (newHardwareVersion != currentHardware)
  {
    // and make the controller reflect the new hardware.
    changeHardwareVersionTo(newHardwareVersion);
  }
}

public void serialEvent(Serial myPort) 
{ 
  // read the serial buffer:
  String incoming = myPort.readStringUntil('\n');
  myPort.clear();
  // if you got any bytes other than the linefeed:
  incoming = trim(incoming);
  println("incoming: " + incoming);
  
  if (incoming.startsWith("READY"))
  {
    drawbotReady = true;
    setHardwareVersionFromIncoming(incoming);
  }
  else if (incoming.startsWith("SYNC"))
    readMachinePosition(incoming);
  else if (incoming.startsWith("CARTESIAN"))
    readCartesianMachinePosition(incoming);
  else if (incoming.startsWith("PGNAME"))
    readMachineName(incoming);
  else if (incoming.startsWith("PGSIZE"))
    readMachineSize(incoming);
  else if (incoming.startsWith("PGMMPERREV"))
    readMmPerRev(incoming);
  else if (incoming.startsWith("PGSTEPSPERREV"))
    readStepsPerRev(incoming);
  else if (incoming.startsWith("PGSTEPMULTIPLIER"))
    readStepMultiplier(incoming);
  else if (incoming.startsWith("PGLIFT"))
    readPenLiftRange(incoming);
  else if (incoming.startsWith("PGSPEED"))
    readMachineSpeed(incoming);
    
  else if ("RESEND".equals(incoming))
    resendLastCommand();
  else if ("DRAWING".equals(incoming))
    drawbotReady = false;
  else if (incoming.startsWith("MEMORY"))
    extractMemoryUsage(incoming);

  if (drawbotReady)
    drawbotConnected = true;
}

public void extractMemoryUsage(String mem)
{
  String[] splitted = split(mem, ",");
  if (splitted.length == 3)
  {
    machineAvailMem = Integer.parseInt(splitted[1]);
    machineUsedMem = currentSram - machineAvailMem;
    if (machineAvailMem < machineMinAvailMem)
      machineMinAvailMem = machineAvailMem;
  }
}

public void readMachinePosition(String sync)
{
  String[] splitted = split(sync, ",");
  if (splitted.length == 4)
  {
    String currentAPos = splitted[1];
    String currentBPos = splitted[2];
    Float a = Float.valueOf(currentAPos).floatValue();
    Float b = Float.valueOf(currentBPos).floatValue();
    currentMachinePos.x = a;
    currentMachinePos.y = b;  
    currentMachinePos = getDisplayMachine().inMM(getDisplayMachine().asCartesianCoords(currentMachinePos));
  }
}
public void readCartesianMachinePosition(String sync)
{
  String[] splitted = split(sync, ",");
  if (splitted.length == 4)
  {
    String currentAPos = splitted[1];
    String currentBPos = splitted[2];
    Float a = Float.valueOf(currentAPos).floatValue();
    Float b = Float.valueOf(currentBPos).floatValue();
    currentCartesianMachinePos.x = a;
    currentCartesianMachinePos.y = b;  
  }
}

public void readMmPerRev(String in)
{
  String[] splitted = split(in, ",");
  if (splitted.length == 3)
  {
    String mmStr = splitted[1];
    
    float mmPerRev = Float.parseFloat(mmStr);
    getDisplayMachine().setMMPerRev(mmPerRev);
    updateNumberboxValues();
  }
}

public void readStepsPerRev(String in)
{
  String[] splitted = split(in, ",");
  if (splitted.length == 3)
  {
    String stepsStr = splitted[1];
    
    Float stepsPerRev = Float.parseFloat(stepsStr);
    getDisplayMachine().setStepsPerRev(stepsPerRev);
    updateNumberboxValues();
  }
}

public void readStepMultiplier(String in)
{
  String[] splitted = split(in, ",");
  if (splitted.length == 3)
  {
    String stepsStr = splitted[1];
    
    machineStepMultiplier = Integer.parseInt(stepsStr);
    updateNumberboxValues();
  }
}


public void readMachineSize(String in)
{
  String[] splitted = split(in, ",");
  if (splitted.length == 4)
  {
    String mWidth = splitted[1];
    String mHeight = splitted[2];
    
    Integer intWidth = Integer.parseInt(mWidth);
    Integer intHeight = Integer.parseInt(mHeight);
    
    float fWidth = getDisplayMachine().inSteps(intWidth);
    float fHeight = getDisplayMachine().inSteps(intHeight);
    
    getDisplayMachine().setSize(PApplet.parseInt(fWidth+0.5f), PApplet.parseInt(fHeight+0.5f));
    updateNumberboxValues();
  }
}

public void readMachineName(String sync)
{
  String[] splitted = split(sync, ",");
  if (splitted.length == 3)
  {
    String name = splitted[1];
    
  }
}

public void readMachineSpeed(String in)
{
  String[] splitted = split(in, ",");
  if (splitted.length == 4)
  {
    String speed = splitted[1];
    String accel = splitted[2];
    
    currentMachineMaxSpeed = Float.parseFloat(speed);
    currentMachineAccel = Float.parseFloat(accel);
    
    updateNumberboxValues();
  }
}

public void readPenLiftRange(String in)
{
  String[] splitted = split(in, ",");
  if (splitted.length == 4)
  {
    String downPos = splitted[1];
    String upPos = splitted[2];
    
    penLiftDownPosition = Integer.parseInt(downPos);
    penLiftUpPosition = Integer.parseInt(upPos);

    updateNumberboxValues();
  }
}

public void resendLastCommand()
{
  println("Re-sending command: " + lastCommand);
  myPort.write(lastCommand);
  drawbotReady = false;
}

public void dispatchCommandQueue()
{
  if (isDrawbotReady() 
    && (!commandQueue.isEmpty() || !realtimeCommandQueue.isEmpty())
    && commandQueueRunning)
  {
    if (pixelTimerRunning)
    {
      timeLastPixelStarted = new Date();
      numberOfPixelsCompleted++;
    }

    if (!realtimeCommandQueue.isEmpty())
    {
      String command = realtimeCommandQueue.get(0);
      lastCommand = command;
      realtimeCommandQueue.remove(0);
      println("Dispatching PRIORITY command: " + command);
    }
    else
    {
      String command = commandQueue.get(0);
      lastCommand = command;
      commandQueue.remove(0);
      println("Dispatching command: " + command);
    }
    Checksum crc = new CRC32();
    crc.update(lastCommand.getBytes(), 0, lastCommand.length());
    lastCommand = lastCommand+":"+crc.getValue();
    println("Last command:" + lastCommand);
    myPort.write(lastCommand);
    drawbotReady = false;
  }
  else if (commandQueue.isEmpty())
  {
    stopPixelTimer();
  }  
}

public void addToCommandQueue(String command)
{
  synchronized (commandQueue)
  {
    commandQueue.add(command);
  }
}
public synchronized void addToRealtimeCommandQueue(String command)
{
  synchronized (realtimeCommandQueue)
  {
    realtimeCommandQueue.add(command);
  }
}

public void startPixelTimer()
{
  timerStart = new Date();
  timeLastPixelStarted = timerStart;
  pixelTimerRunning = true;
}
public void stopPixelTimer()
{
  pixelTimerRunning = false;
}

public boolean isDrawbotReady()
{
  return drawbotReady;
}
public boolean isDrawbotConnected()
{
  return drawbotConnected;
}

public Properties getProperties()
{
  if (props == null)
  {
    FileInputStream propertiesFileStream = null;
    try
    {
      props = new Properties();
      String fileToLoad = sketchPath(propertiesFilename);
      
      File propertiesFile = new File(fileToLoad);
      if (!propertiesFile.exists())
      {
        println("saving.");
        savePropertiesFile();
        println("saved.");
      }
      
      propertiesFileStream = new FileInputStream(propertiesFile);
      props.load(propertiesFileStream);
      println("Successfully loaded properties file " + fileToLoad);
    }
    catch (IOException e)
    {
      println("Couldn't read the properties file - will attempt to create one.");
      println(e.getMessage());
    }
    finally
    {
      try 
      { 
        propertiesFileStream.close();
      }
      catch (Exception e) 
      {
        println("Exception: "+e.getMessage());
      };
    }
  }
  return props;
}

public void loadFromPropertiesFile()
{
  getDisplayMachine().loadDefinitionFromProperties(getProperties());
  this.pageColour = getColourProperty("controller.page.colour", color(220));
  this.frameColour = getColourProperty("controller.frame.colour", color(200,0,0));
  this.machineColour = getColourProperty("controller.machine.colour", color(150));
  this.guideColour = getColourProperty("controller.guide.colour", color(255));
  this.backgroundColour = getColourProperty("controller.background.colour", color(100));
  this.densityPreviewColour = getColourProperty("controller.densitypreview.colour", color(0));
  this.chromaKeyColour = getColourProperty("controller.pixel.mask.color", color(0,255,0));

  // pen size
  this.currentPenWidth = getFloatProperty("machine.pen.size", 0.8f);

  // motor settings
  this.currentMachineMaxSpeed = getFloatProperty("machine.motors.maxSpeed", 600.0f);
  this.currentMachineAccel = getFloatProperty("machine.motors.accel", 400.0f);
  this.machineStepMultiplier = getIntProperty("machine.step.multiplier", 1);
  
  // serial port
  this.serialPortNumber = getIntProperty("controller.machine.serialport", 0);
  this.baudRate = getIntProperty("controller.machine.baudrate", 57600);

  // row size
  this.gridSize = getFloatProperty("controller.grid.size", 100.0f);
  this.sampleArea = getIntProperty("controller.pixel.samplearea", 2);
  this.pixelScalingOverGridSize = getFloatProperty("controller.pixel.scaling", 1.0f);
  
  // pixel renderer
  this.densityPreviewStyle = getIntProperty("controller.density.preview.style", 1);
  
  // initial screen size
  this.windowWidth = getIntProperty("controller.window.width", 650);
  this.windowHeight = getIntProperty("controller.window.height", 400);
  
  println("windowHeight:" + this.windowHeight);

  this.testPenWidthStartSize = getFloatProperty("controller.testPenWidth.startSize", 0.5f);
  this.testPenWidthEndSize = getFloatProperty("controller.testPenWidth.endSize", 2.0f);
  this.testPenWidthIncrementSize = getFloatProperty("controller.testPenWidth.incrementSize", 0.5f);
  
  this.maxSegmentLength = getIntProperty("controller.maxSegmentLength", 2);
  
  float homePointX = getFloatProperty("controller.homepoint.x", 0.0f);
  float homePointY = getFloatProperty("controller.homepoint.y", 0.0f);
  
  if (homePointX == 0.0f)
  {
    float defaultX = getDisplayMachine().getWidth() / 2.0f;    // in steps
    float defaultY = getDisplayMachine().getPage().getTop();  // in steps
//    homePointX = getDisplayMachine().inMM(defaultX);
//    homePointY = getDisplayMachine().inMM(defaultY);
    println("Loading default homepoint.");
  }
  this.homePointCartesian = new PVector(getDisplayMachine().inSteps(homePointX), getDisplayMachine().inSteps(homePointY));
//  println("home point loaded: " + homePointCartesian + ", " + getHomePoint());
  
  setVectorFilename(getStringProperty("controller.vector.filename", null));
  if (getVectorFilename() != null)
  {
    RShape shape = null;
    try
    {
      shape = RG.loadShape(getVectorFilename());
    }
    catch (Exception e)
    {
      shape = null;
    }
    
    if (shape != null) 
    {
      setVectorShape(shape);
    }
    else 
    {
      println("File not found (" + getVectorFilename() + ")");
    }
  }
  vectorScaling = getFloatProperty("controller.vector.scaling", 100.0f);
  getVectorPosition().x = getFloatProperty("controller.vector.position.x", 0.0f);
  getVectorPosition().y = getFloatProperty("controller.vector.position.y", 0.0f);
  this.minimumVectorLineLength = getIntProperty("controller.vector.minLineLength", 0);


  
  println("Finished loading configuration from properties file.");
}

public void savePropertiesFile()
{
  Properties props = new Properties();
  
  props = getDisplayMachine().loadDefinitionIntoProperties(props);

  props.setProperty("controller.page.colour", hex(this.pageColour, 6));
  props.setProperty("controller.frame.colour", hex(this.frameColour,6));
  props.setProperty("controller.machine.colour", hex(this.machineColour,6));
  props.setProperty("controller.guide.colour", hex(this.guideColour,6));
  props.setProperty("controller.background.colour", hex(this.backgroundColour,6));
  props.setProperty("controller.densitypreview.colour", hex(this.densityPreviewColour,6));

  
  // pen size
  props.setProperty("machine.pen.size", new Float(currentPenWidth).toString());
  // serial port
  props.setProperty("controller.machine.serialport", getSerialPortNumber().toString());
  props.setProperty("controller.machine.baudrate", getBaudRate().toString());

  // row size
  props.setProperty("controller.grid.size", new Float(gridSize).toString());
  props.setProperty("controller.pixel.samplearea", new Float(sampleArea).toString());
  props.setProperty("controller.pixel.scaling", new Float(pixelScalingOverGridSize).toString());

  // density preview style
  props.setProperty("controller.density.preview.style", new Integer(getDensityPreviewStyle()).toString());

  // initial screen size
  props.setProperty("controller.window.width", new Integer((windowWidth < 50) ? 50 : windowWidth-16).toString());
  props.setProperty("controller.window.height", new Integer((windowWidth < 50) ? 50 : windowHeight-38).toString());

  props.setProperty("controller.testPenWidth.startSize", new Float(testPenWidthStartSize).toString());
  props.setProperty("controller.testPenWidth.endSize", new Float(testPenWidthEndSize).toString());
  props.setProperty("controller.testPenWidth.incrementSize", new Float(testPenWidthIncrementSize).toString());
  
  props.setProperty("controller.maxSegmentLength", new Integer(getMaxSegmentLength()).toString());
  
  props.setProperty("machine.motors.maxSpeed", new Float(currentMachineMaxSpeed).toString());
  props.setProperty("machine.motors.accel", new Float(currentMachineAccel).toString());
  props.setProperty("machine.step.multiplier", new Integer(machineStepMultiplier).toString());
  
  props.setProperty("controller.pixel.mask.color", hex(this.chromaKeyColour, 6));
  
  PVector hp = null;  
  if (getHomePoint() != null)
  {
    hp = getHomePoint();
  }
  else
    hp = new PVector(2000.0f, 1000.0f);
    
  hp = getDisplayMachine().inMM(hp);
  
  props.setProperty("controller.homepoint.x", new Float(hp.x).toString());
  props.setProperty("controller.homepoint.y", new Float(hp.y).toString());
  
  if (getVectorFilename() != null)
    props.setProperty("controller.vector.filename", getVectorFilename());
    
  props.setProperty("controller.vector.scaling", new Float(vectorScaling).toString());
  props.setProperty("controller.vector.position.x", new Float(getVectorPosition().x).toString());
  props.setProperty("controller.vector.position.y", new Float(getVectorPosition().y).toString());
  props.setProperty("controller.vector.minLineLength", new Integer(this.minimumVectorLineLength).toString());

 
  FileOutputStream propertiesOutput = null;

  try
  {
    //save the properties to a file
    File propertiesFile = new File(sketchPath(propertiesFilename));
    if (propertiesFile.exists())
    {
      propertiesOutput = new FileOutputStream(propertiesFile);
      Properties oldProps = new Properties();
      FileInputStream propertiesFileStream = new FileInputStream(propertiesFile);
      oldProps.load(propertiesFileStream);
      oldProps.putAll(props);
      oldProps.store(propertiesOutput,"   ***  Polargraph properties file   ***  ");
      println("Saved settings.");
    }
    else
    { // create it
      propertiesFile.createNewFile();
      propertiesOutput = new FileOutputStream(propertiesFile);
      props.store(propertiesOutput,"   ***  Polargraph properties file   ***  ");
      println("Created file.");
    }
  }
  catch (Exception e)
  {
    println("Exception occurred while creating new properties file: " + e.getMessage());
  }
  finally
  {
    if (propertiesOutput != null)
    {
      try
      {
        propertiesOutput.close();
      }
      catch (Exception e2) {println("what now!"+e2.getMessage());}
    }
  }
}

public boolean getBooleanProperty(String id, boolean defState) 
{
  return PApplet.parseBoolean(getProperties().getProperty(id,""+defState));
}
 
public int getIntProperty(String id, int defVal) 
{
  return PApplet.parseInt(getProperties().getProperty(id,""+defVal)); 
}
 
public float getFloatProperty(String id, float defVal) 
{
  return PApplet.parseFloat(getProperties().getProperty(id,""+defVal)); 
}
public String getStringProperty(String id, String defVal)
{
  return getProperties().getProperty(id, defVal);
}
public int getColourProperty(String id, int defVal)
{
  int col = color(180);
  String colStr = getProperties().getProperty(id, "");
  if ("".equals(colStr))
  {
    col = defVal;
  }
  
  if (colStr.length() == 1)
  {
    // single value grey
    colStr = colStr+colStr;
    col = color(unhex(colStr));
  }
  else if (colStr.length() == 3)
  {
    // 3 digit rgb
    String d1 = colStr.substring(0,1);
    String d2 = colStr.substring(1,2);
    String d3 = colStr.substring(2,3);
    d1 = d1+d1;
    d2 = d2+d2;
    d3 = d3+d3;
    
    col = color(unhex(d1), unhex(d2), unhex(d3));
  }
  else if  (colStr.length() == 6)
  {
    // 6 digit rgb
    String d1 = colStr.substring(0,2);
    String d2 = colStr.substring(2,4);
    String d3 = colStr.substring(4,6);
    
    col = color(unhex(d1), unhex(d2), unhex(d3));
  }
  
  return col;
}

public Integer getSerialPortNumber()
{
  return this.serialPortNumber;
}
public String getStoreFilename()
{
  return this.storeFilename;
}
public void setStoreFilename(String filename)
{
  this.storeFilename = filename;
}

public boolean getOverwriteExistingStoreFile()
{
  return this.overwriteExistingStoreFile;
}
public void setOverwriteExistingStoreFile(boolean over)
{
  this.overwriteExistingStoreFile = over;
}
  
public void initProperties()
{
  getProperties();
}

public PVector getVectorPosition()
{
  return vectorPosition;
}

public float getPixelScalingOverGridSize()
{
  return pixelScalingOverGridSize;
}
public void setPixelScalingOverGridSize(float scaling)
{
  pixelScalingOverGridSize = scaling;
}
public int getDensityPreviewStyle()
{
  return densityPreviewStyle;
}

public Integer getBaudRate()
{
  return baudRate;
}
public boolean isUseWindowedConsole()
{
  return this.useWindowedConsole;
}
public void setUseWindowedConsole(boolean use)
{
  this.useWindowedConsole = use;
}
public void initLogging()
{
  try
  {
//    logger = Logger.getLogger("uk.co.polargraph.controller");
//    FileHandler fileHandler = new FileHandler("mylog.txt");
//    fileHandler.setFormatter(new SimpleFormatter());
//    logger.addHandler(fileHandler);
//    logger.setLevel(Level.INFO);
//    logger.info("Hello");
    if (isUseWindowedConsole())
    {
      console = new Console();
    }
    else
    {
      console.close();
      console = null;
    }
  }
  catch(Exception e)
  {
    println("Exception setting up logger: " + e.getMessage());
  }
}
public void initImages()
{
//  try
//  {
//    yButtonImage = loadImage("y.png");
//    xButtonImage = loadImage("x.png");
//    aButtonImage = loadImage("a.png");
//    bButtonImage = loadImage("b.png");
//    dpadXImage = loadImage("dpadlr.png");
//    dpadYImage = loadImage("dpadud.png");
//  }
//  catch (Exception e)
//  {
//    yButtonImage = makeColourImage(64,64,color(180,180,0));
//    xButtonImage = makeColourImage(64,64,color(0,0,180));
//    aButtonImage = makeColourImage(64,64,color(0,180,0));
//    bButtonImage = makeColourImage(64,64,color(180,0,0));
//  }
}

public PImage makeColourImage(int w, int h, int colour)
{
  PImage img = createImage(w,h,RGB);
  for(int i=0; i < img.pixels.length; i++) {
    img.pixels[i] = colour; 
  }
  return img;
}

public final class BLOBable_blueBlobs implements BLOBable{
  private PImage img_;
  int col = color(0, 0, 255);
  
  public BLOBable_blueBlobs(PImage img){
    img_ = img;
  }
  
  //@Override
  public final void init() {
  }
  
  //@Override
  public final void updateOnFrame(int width, int height) {
  }
  //@Override
  public final boolean isBLOBable(int pixel_index, int x, int y) {
    if( img_.pixels[pixel_index] ==  col){
      return true;
    } else {
      return false;
    }
  }
}
/**
 Polargraph controller
 Copyright Sandy Noble 2012.
 
 This file is part of Polargraph Controller.
 
 Polargraph Controller is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Polargraph Controller is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
 
 Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
 Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
 
 This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.
 
 sandy.noble@gmail.com
 http://www.polargraph.co.uk/
 http://code.google.com/p/polargraph/
 */

class DisplayMachine extends Machine
{
  private Rectangle outline = null;
  private float scaling = 1.0f;
  private Scaler scaler = null;
  private PVector offset = null;
  private float imageTransparency = 1.0f;

  private Set<PVector> extractedPixels = new HashSet<PVector>(0);

  PImage scaledImage = null;
  
  private PVector currentPixel = null;

  public DisplayMachine(Machine m, PVector offset, float scaling)
  {
    // construct
    super(m.getWidth(), m.getHeight(), m.getMMPerRev(), m.getStepsPerRev());

    super.machineSize = m.machineSize;

    super.page = m.page;
    super.imageFrame = m.imageFrame;
    super.pictureFrame = m.pictureFrame;

    super.imageBitmap = m.imageBitmap;
    super.imageFilename = m.imageFilename;

    super.stepsPerRev = m.stepsPerRev;
    super.mmPerRev = m.mmPerRev;

    super.mmPerStep = m.mmPerStep;
    super.stepsPerMM = m.stepsPerMM;
    super.maxLength = m.maxLength;
    super.gridSize = m.gridSize;

    this.offset = offset;
    this.scaling = scaling;
    this.scaler = new Scaler(scaling, 100.0f);

    this.outline = null;
  }

  public Rectangle getOutline()
  {
    outline = new Rectangle(offset, new PVector(sc(super.getWidth()), sc(super.getHeight())));
    return this.outline;
  }

  private Scaler getScaler()
  {
    if (scaler == null)
      this.scaler = new Scaler(getScaling(), getMMPerStep());
    return scaler;
  }

  public void setScale(float scale)
  {
    this.scaling = scale;
    this.scaler = new Scaler(scale, getMMPerStep());
  }
  public float getScaling()
  {
    return this.scaling;
  }
  public float sc(float val)
  {
    return getScaler().scale(val);
  }
  public void setOffset(PVector offset)
  {
    this.offset = offset;
  }
  public PVector getOffset()
  {
    return this.offset;
  }
  public void setImageTransparency(float trans)
  {
    this.imageTransparency = trans;
  }
  public int getImageTransparency()
  {
    float f = 255.0f * this.imageTransparency;
    f += 0.5f;
    int result = (int) f;
    return result;
  }
  
  public PVector getCurrentPixel()
  {
    return this.currentPixel;
  }
  public void setCurrentPixel(PVector p)
  {
    this.currentPixel = p;
  }

  public void loadNewImageFromFilename(String filename)
  {
    super.loadImageFromFilename(filename);
    super.sizeImageFrameToImageAspectRatio();
    this.setExtractedPixels(new HashSet<PVector>(0));
  }

  public final int DROP_SHADOW_DISTANCE = 4;
  public String getZoomText()
  {
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
    DecimalFormat df = (DecimalFormat)nf;  
    df.applyPattern("###");
    String zoom = df.format(scaling * 100) + "% zoom";
    return zoom;
  }

  public String getDimensionsAsText(Rectangle r)
  {
    return getDimensionsAsText(r.getSize());
  }
  public String getDimensionsAsText(PVector p)
  {
    String dim = inMM(p.x) + " x " + inMM(p.y) + "mm";
    return dim;
  }

  public void drawForSetup()
  {
    // work out the scaling factor.
    noStroke();
    // draw machine outline

    // drop shadow
    fill(80);
    rect(getOutline().getLeft()+DROP_SHADOW_DISTANCE, getOutline().getTop()+DROP_SHADOW_DISTANCE, getOutline().getWidth(), getOutline().getHeight());

    fill(getMachineColour());
    rect(getOutline().getLeft(), getOutline().getTop(), getOutline().getWidth(), getOutline().getHeight());
    text("machine " + getDimensionsAsText(getSize()) + " " + getZoomText(), getOutline().getLeft(), getOutline().getTop());

    if (displayingGuides)
    {
      // draw some guides
      stroke(getGuideColour());
      strokeWeight(1);
      // centre line
      line(getOutline().getLeft()+(getOutline().getWidth()/2), getOutline().getTop(), 
      getOutline().getLeft()+(getOutline().getWidth()/2), getOutline().getBottom());

      // page top line
      line(getOutline().getLeft(), getOutline().getTop()+sc(getHomePoint().y), 
      getOutline().getRight(), getOutline().getTop()+sc(getHomePoint().y));
    }

    // draw page
    fill(getPageColour());
    rect(getOutline().getLeft()+sc(getPage().getLeft()), 
    getOutline().getTop()+sc(getPage().getTop()), 
    sc(getPage().getWidth()), 
    sc(getPage().getHeight()));
    text("page " + getDimensionsAsText(getPage()), getOutline().getLeft()+sc(getPage().getLeft()), 
    getOutline().getTop()+sc(getPage().getTop()));
    fill(0);
    text("offset " + getDimensionsAsText(getPage().getPosition()), 
    getOutline().getLeft()+sc(getPage().getLeft()), 
    getOutline().getTop()+sc(getPage().getTop())+10);
    noFill();

    // draw home point
    noFill();
    strokeWeight(5);
    stroke(0, 128);
    PVector onScreen = scaleToScreen(inMM(getHomePoint()));
    ellipse(onScreen.x, onScreen.y, 15, 15);
    strokeWeight(2);
    stroke(255);
    ellipse(onScreen.x, onScreen.y, 15, 15);
    
    text("Home point", onScreen.x+ 15, onScreen.y-5);
    text(PApplet.parseInt(inMM(getHomePoint().x)+0.5f) + ", " + PApplet.parseInt(inMM(getHomePoint().y)+0.5f), onScreen.x+ 15, onScreen.y+15);


    if (displayingGuides 
      && getOutline().surrounds(getMouseVector())
      && currentMode != MODE_MOVE_IMAGE
      && mouseOverControls().isEmpty()
      )
    {  
      drawHangingStrings();
      drawLineLengthTexts();
      cursor(CROSS);
    }
    else
    {
      cursor(ARROW);
    }
  }

  public void drawLineLengthTexts()
  {
    PVector actual = inMM(asNativeCoords(inSteps(scaleToDisplayMachine(getMouseVector()))));
    PVector cart = scaleToDisplayMachine(getMouseVector());
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
    DecimalFormat df = (DecimalFormat)nf;  
    df.applyPattern("###.#");

    text("Line 1: " + df.format(actual.x) + "mm", getDisplayMachine().getOutline().getLeft()+10, getDisplayMachine().getOutline().getTop()+18);
    text("Line 2: " + df.format(actual.y) + "mm", getDisplayMachine().getOutline().getLeft()+10, getDisplayMachine().getOutline().getTop()+28);

    text("X Position: " + df.format(cart.x) + "mm", getDisplayMachine().getOutline().getLeft()+10, getDisplayMachine().getOutline().getTop()+42);
    text("Y Position: " + df.format(cart.y) + "mm", getDisplayMachine().getOutline().getLeft()+10, getDisplayMachine().getOutline().getTop()+52);
  }

  public void draw()
  {
    // work out the scaling factor.
    noStroke();
    // draw machine outline

//    fill(80);
//    rect(getOutline().getLeft()+DROP_SHADOW_DISTANCE, getOutline().getTop()+DROP_SHADOW_DISTANCE, getOutline().getWidth(), getOutline().getHeight());

    fill(getMachineColour());
    rect(getOutline().getLeft(), getOutline().getTop(), getOutline().getWidth(), getOutline().getHeight());



    if (displayingGuides)
    {
      // draw some guides
      stroke(getGuideColour());
      strokeWeight(1);
      // centre line
      line(getOutline().getLeft()+(getOutline().getWidth()/2), getOutline().getTop(), 
      getOutline().getLeft()+(getOutline().getWidth()/2), getOutline().getBottom());

      // page top line
      line(getOutline().getLeft(), getOutline().getTop()+sc(getHomePoint().y), 
      getOutline().getRight(), getOutline().getTop()+sc(getHomePoint().y));
    }

    // draw page
    fill(getPageColour());
    rect(getOutline().getLeft()+sc(getPage().getLeft()), 
    getOutline().getTop()+sc(getPage().getTop()), 
    sc(getPage().getWidth()), 
    sc(getPage().getHeight()));
    text("page " + getDimensionsAsText(getPage()), getOutline().getLeft()+sc(getPage().getLeft()), 
    getOutline().getTop()+sc(getPage().getTop())-3);
    noFill();



    // draw actual image
    if (displayingImage && imageIsReady())
    {
      float ox = getOutline().getLeft()+sc(getImageFrame().getLeft());
      float oy = getOutline().getTop()+sc(getImageFrame().getTop());
      float w = sc(getImageFrame().getWidth());
      float h = sc(getImageFrame().getHeight());
      tint(255, getImageTransparency());
      image(getImage(), ox, oy, w, h);
      noTint();
      strokeWeight(1);
      stroke(150, 150, 150, 40);
      rect(ox, oy, w-1, h-1);
      fill(150, 150, 150, 40);
      text("image", ox, oy-3);
      noFill();
    }
    
    stroke(getBackgroundColour(),150);
    strokeWeight(3);
    noFill();
    rect(getOutline().getLeft()-2, getOutline().getTop()-2, getOutline().getWidth()+3, getOutline().getHeight()+3);

    stroke(getMachineColour(),150);
    strokeWeight(3);
    noFill();
    rect(getOutline().getLeft()+sc(getPage().getLeft())-2, 
    getOutline().getTop()+sc(getPage().getTop())-2, 
    sc(getPage().getWidth())+4, 
    sc(getPage().getHeight())+4);



    if (displayingSelectedCentres)
    {
      drawExtractedPixelCentres();
    }
    if (displayingDensityPreview)
    {
      drawExtractedPixelDensities();
    }
    if (displayingGuides)
    {
      drawPictureFrame();
    }

    if (displayingVector && getVectorShape() != null)
    {
      displayVectorImage();
    }

    if (displayingGuides 
      && getOutline().surrounds(getMouseVector())
      && currentMode != MODE_MOVE_IMAGE
      && mouseOverControls().isEmpty()
      )
    {
      drawHangingStrings();
      drawRows();
      cursor(CROSS);
    }
    else
    {
      cursor(ARROW);
    }
  }
  
  public void drawForTrace()
  {
    // work out the scaling factor.
    noStroke();
    // draw machine outline
    
//    liveImage = trace_buildLiveImage();
    // draw actual image

//    if (drawingLiveVideo)
//    {
//      displayLiveVideo();
//    }
    
    if (drawingTraceShape && traceShape != null)
    {
      displaytraceShape();
    }
    else
    {
      
    }
  }
  
//  public void displayLiveVideo()
//  {
//    // draw actual image, maximum size
//    if (processedLiveImage != null)
//    {
//      // origin - top left of the corner
//      float ox = getPanel(PANEL_NAME_WEBCAM).getOutline().getRight()+7;
//      float oy = getPanel(PANEL_NAME_GENERAL).getOutline().getTop();
//      
//      // calculate size to display at.
//      float aspectRatio = (rotateWebcamImage) ? 480.0/640.0 : 640.0/480.0; // rotated, remember
//      float h = height - getPanel(PANEL_NAME_GENERAL).getOutline().getTop() -10;
//      float w = h * (480.0/640.0);
////      println("height: " + h + ", width: " + w);
////      println("origin x: " + ox + ", y: " + oy);
//      
//      if (rotateWebcamImage) 
//      {
//        float t = h;
//        h = w;
//        w = t;
//      }
//      
//      //stroke(255);
//      rect(ox,oy,w,h);
//
//      tint(255, getImageTransparency());
//      if (rotateWebcamImage)
//      {
//        translate(ox, oy);
//        rotate(radians(270));
//        image(processedLiveImage, -w, 0, w, h);
//        image(liveImage, -w, (w-(w/4))+10, w/4, h/4);
////        stroke(0,255,0);
////        ellipse(0,0,80,40);
////        stroke(0,0,255);
////        ellipse(-w,0,80,40);
//        rotate(radians(-270));
//        translate(-ox, -oy);
//      }
//      else
//      {
//        translate(ox, oy);
//        image(processedLiveImage, 0, 0, h, w);
//        image(liveImage, h-(h/4), w+10, h/4, w/4);
//        translate(-ox, -oy);
//      }
//      noTint();
//      noFill();
//    }
//  }
  
  public void displaytraceShape()
  {
    strokeWeight(1);
    
    if (captureShape != null)
    {
      //displaytraceShapeAtFullSize(traceShape, false, color(150,150,150));
      displaytraceShapeAtFullSize(captureShape, true, color(0,0,0));
    }
    else
    {
      displaytraceShapeAtFullSize(traceShape, false, color(255,255,255));
    }
  }
  
  public void displaytraceShapeAtFullSize(RShape vec, boolean illustrateSequence, Integer colour)
  {
    RG.ignoreStyles();
    // work out scaling to make it full size on the screen
    float aspectRatio = vec.getWidth()/vec.getHeight(); // rotated, remember
    float h = height - getPanel(PANEL_NAME_GENERAL).getOutline().getTop() -10;
    float w = h * aspectRatio;
    float scaler = h / vec.getWidth();
    if (rotateWebcamImage)
      scaler =  h / vec.getHeight();
    PVector position = new PVector(getPanel(PANEL_NAME_TRACE).getOutline().getRight()+7, getPanel(PANEL_NAME_GENERAL).getOutline().getTop());

    noFill();
    RPoint[][] pointPaths = vec.getPointsInPaths();
    if (illustrateSequence)
      pointPaths = sortPathsCentreFirst(vec, pathLengthHighPassCutoff);
    
    if (pointPaths != null)
    {
      float incPerPath = 0.0f;
      if (illustrateSequence)
        incPerPath = 255.0f / (float) pointPaths.length;

      for(int i = 0; i<pointPaths.length; i++)
      {
        float col = (float)i * incPerPath;
//        if (pointPaths[i].length >= pathLengthHighPassCutoff)
//        {
          if (pointPaths[i] != null) 
          {
            if (illustrateSequence)
              stroke((int)col, (int)col, (int)col, 128);
            else
              stroke(colour);
              
            beginShape();
            for (int j = 0; j<pointPaths[i].length; j++)
            {
              PVector p = new PVector(pointPaths[i][j].x, pointPaths[i][j].y);
              p = PVector.mult(p, scaler);
              p = PVector.add(p, position);
              vertex(p.x, p.y);
            }
            endShape();
//          }
        }
      }
    }
    noFill();
  }
  
  public void displayVectorImage()
  {
    displayVectorImage(getVectorShape(), vectorScaling/100, getVectorPosition(), color(0,0,0), true);
    
    if (captureShape != null)
    {
      float scaling = inMM(getPictureFrame().getWidth()) / captureShape.getWidth();
      PVector position = new PVector(inMM(getPictureFrame().getPosition().x), inMM(getPictureFrame().getPosition().y) + (captureShape.getHeight() * scaling));
      displayVectorImage(captureShape, 
        scaling, 
        position, 
        color(0,200,0), true);
    }
  }
  
  public void displayVectorImage(RShape vec, float scaling, PVector position, int strokeColour, boolean drawCentroid)
  {
    PVector centroid = new PVector(vec.width/2, vec.height/2);
    centroid = PVector.mult(centroid, (vectorScaling/100));
    centroid = PVector.add(centroid, getVectorPosition());
    centroid = scaleToScreen(centroid);

    RPoint[][] pointPaths = vec.getPointsInPaths();
    RG.ignoreStyles();
    strokeWeight(1);
    if (pointPaths != null)
    {
      for(int i = 0; i<pointPaths.length; i++)
      {
        if (pointPaths[i] != null) 
        {
          beginShape();
          for (int j = 0; j<pointPaths[i].length; j++)
          {
            PVector p = new PVector(pointPaths[i][j].x, pointPaths[i][j].y);
            p = PVector.mult(p, scaling);
            p = PVector.add(p, position);
            if (getPage().surrounds(inSteps(p)))
            {
              p = scaleToScreen(p);
              stroke(strokeColour);
              vertex(p.x, p.y);
              //ellipse(p.x, p.y, 3, 3);
            }
          }
          endShape();
        }
      }
      if (drawCentroid)
      {
        // draw spot at centre
        fill(255,0,0,128);
        ellipse(centroid.x, centroid.y, 20,20);
        noFill();
      }
    }
  }


  // this scales a value from the screen to be a position on the machine
  /**  Given a point on-screen, this works out where on the 
   actual machine it refers to.
   */
  public PVector scaleToDisplayMachine(PVector screen)
  {
    // offset
    float x = screen.x - getOffset().x;
    float y = screen.y - getOffset().y;

    // transform
    float scalingFactor = 1.0f/getScaling();
    x = scalingFactor * x;
    y = scalingFactor * y;

    // and out
    PVector mach = new PVector(x, y);
    return mach;
  }

  /** This works out the position, on-screen of a specific point on the machine.
   Both values are cartesian coordinates.
   */
  public PVector scaleToScreen(PVector mach)
  {
    // transform
    float x = mach.x * scaling;
    float y = mach.y * scaling;

    // offset
    x = x + getOffset().x;
    y = y + getOffset().y;

    // and out!
    PVector screen = new PVector(x, y);
    return screen;
  }

  // converts a cartesian coord into a native one
  public PVector convertToNative(PVector cart)
  {
    // width of machine in mm
    float width = inMM(super.getWidth());

    // work out distances
    float a = dist(0, 0, cart.x, cart.y);
    float b = dist(width, 0, cart.x, cart.y);

    // and out
    PVector nativeMM = new PVector(a, b);
    return nativeMM;
  }

  public void drawPictureFrame()
  {
    strokeWeight(1);

    PVector topLeft = scaleToScreen(inMM(getPictureFrame().getTopLeft()));
    PVector botRight = scaleToScreen(inMM(getPictureFrame().getBotRight()));

    stroke (getFrameColour());

    // top left    
    line(topLeft.x-4, topLeft.y, topLeft.x-10, topLeft.y);
    line(topLeft.x, topLeft.y-4, topLeft.x, topLeft.y-10);

    // top right
    line(botRight.x+4, topLeft.y, botRight.x+10, topLeft.y);
    line(botRight.x, topLeft.y-4, botRight.x, topLeft.y-10);

    // bot right
    line(botRight.x+4, botRight.y, botRight.x+10, botRight.y);
    line(botRight.x, botRight.y+4, botRight.x, botRight.y+10);

    // bot left
    line(topLeft.x-4, botRight.y, topLeft.x-10, botRight.y);
    line(topLeft.x, botRight.y+4, topLeft.x, botRight.y+10);

    stroke(255);


    //    float width = inMM(getPictureFrame().getBotRight().x - getPictureFrame().getTopLeft().x);
    //    println("width: "+ width);
  }


  public void drawHangingStrings()
  {
    // hanging strings
    strokeWeight(4);
    stroke(255, 255, 255, 64);
    line(getOutline().getLeft(), getOutline().getTop(), mouseX, mouseY);
    line(getOutline().getRight(), getOutline().getTop(), mouseX, mouseY);
  }

  /**  This draws on screen, showing an arc highlighting the row that the mouse
   is on.
   */
  public void drawRows()
  {
    PVector mVect = getMouseVector();

    // scale it to  find out the coordinates on the machine that the mouse is pointing at.
    mVect = scaleToDisplayMachine(mVect);
    // convert it to the native coordinates system
    mVect = convertToNative(mVect);
    // snap it to the grid
    mVect = snapToGrid(mVect, getGridSize());
    // scale it back to find out how to represent this on-screen
    mVect = scaleToScreen(mVect);

    // and finally, because scaleToScreen also allows for the machine position (offset), subtract it.
    mVect.sub(getOffset());

    float rowThickness = inMM(getGridSize()) * getScaling();
    rowThickness = (rowThickness < 1.0f) ? 1.0f : rowThickness;
    strokeWeight(rowThickness);
    stroke(150, 200, 255, 50);
    strokeCap(SQUARE);

    float dia = mVect.x*2;
    arc(getOutline().getLeft(), getOutline().getTop(), dia, dia, 0, 1.57079633f);

    dia = mVect.y*2;
    arc(getOutline().getRight(), getOutline().getTop(), dia, dia, 1.57079633f, 3.14159266f);
    
  }

  public void drawExtractedPixelCentres()
  {
    for (PVector cartesianPos : getExtractedPixels())
    {
      // scale em, danno.
      PVector scaledPos = scaleToScreen(cartesianPos);
      strokeWeight(1);
      stroke(255, 0, 0, 128);
      noFill();
      line(scaledPos.x-1, scaledPos.y-1, scaledPos.x+1, scaledPos.y+1);
      line(scaledPos.x-1, scaledPos.y+1, scaledPos.x+1, scaledPos.y-1);
    }
  }

  public void drawExtractedPixelDensities()
  {

    float pixelSize = inMM(getGridSize()) * getScaling();
    pixelSize = (pixelSize < 1.0f) ? 1.0f : pixelSize;

    pixelSize = pixelSize * getPixelScalingOverGridSize();

    if (getExtractedPixels() != null)
    {
      for (PVector cartesianPos : getExtractedPixels())
      {
        if ((cartesianPos.z <= pixelExtractBrightThreshold) && (cartesianPos.z >= pixelExtractDarkThreshold))
        {
          // scale em, danno.
          PVector scaledPos = scaleToScreen(cartesianPos);
          noStroke();
          fill(cartesianPos.z);
          switch (getDensityPreviewStyle())
          {
            case DENSITY_PREVIEW_ROUND: 
              previewRoundPixel(scaledPos, pixelSize, pixelSize);
              break;
            case DENSITY_PREVIEW_DIAMOND:
              previewDiamondPixel(scaledPos, pixelSize, pixelSize, cartesianPos.z);
              break;
            default:
              previewRoundPixel(scaledPos, pixelSize, pixelSize);
              break;
          }
        }
      }
    }
    noFill();
  }
  
  public void previewDiamondPixel(PVector pos, float wide, float high, float brightness)
  {
    wide*=1.4f;
    high*=1.4f;
    // shall I try and draw a diamond here instead? OK! I'll do it! Ha!
    float halfWidth = wide / 2.0f;
    float halfHeight = high / 2.0f;
    fill(0,0,0, 255-brightness);
    quad(pos.x, pos.y-halfHeight, pos.x+halfWidth, pos.y, pos.x, pos.y+halfHeight, pos.x-halfWidth, pos.y);
    
  }
  public void previewNativePixel(PVector pos, float wide, float high)
  {
    // shall I try and draw a diamond here instead? OK! I'll do it! Ha!
    float halfWidth = wide / 2.0f;
    float halfHeight = high / 2.0f;
    quad(pos.x, pos.y-halfHeight, pos.x+halfWidth, pos.y, pos.x, pos.y+halfHeight, pos.x-halfWidth, pos.y);
    
  }
  public void previewRoundPixel(PVector pos, float wide, float high)
  {
     ellipse(pos.x, pos.y, wide*1.1f, high*1.1f);
  }
  
  public int getPixelAtScreenCoords(PVector pos)
  {
    pos = scaleToDisplayMachine(pos);
    pos = inSteps(pos);
    float scalingFactor = getImage().width / getImageFrame().getWidth();
    int col = super.getPixelAtMachineCoords(pos, scalingFactor);
    return col;
  }

  public Set<PVector> getExtractedPixels()
  {
    return this.extractedPixels;
  }
  public void setExtractedPixels(Set<PVector> p)
  {
    this.extractedPixels = p;
  }

  /* This will return a list of pixels that are included in the area in the 
   parameter.  All coordinates are for the screen.
   */
  public Set<PVector> getPixelsPositionsFromArea(PVector p, PVector s, float rowSize)
  {
    extractPixelsFromArea(p, s, rowSize, 0.0f);
    return getExtractedPixels();
  }

  public void extractPixelsFromArea(PVector p, PVector s, float rowSize, float sampleSize)
  {
    // get the native positions from the superclass
    Set<PVector> nativePositions = super.getPixelsPositionsFromArea(inSteps(p), inSteps(s), rowSize, sampleSize);

    // work out the cartesian positions
    Set<PVector> cartesianPositions = new HashSet<PVector>(nativePositions.size());
    for (PVector nativePos : nativePositions)
    {
      // convert to cartesian
      PVector displayPos = super.asCartesianCoords(nativePos);
      displayPos = inMM(displayPos);
      displayPos.z = nativePos.z;
      cartesianPositions.add(displayPos);
    }
    setExtractedPixels(cartesianPositions);
  }


  public Set<PVector> extractNativePixelsFromArea(PVector p, PVector s, float rowSize, float sampleSize)
  {
    // get the native positions from the superclass
    Set<PVector> nativePositions = super.getPixelsPositionsFromArea(inSteps(p), inSteps(s), rowSize, sampleSize);
    return nativePositions;
  }

  protected PVector snapToGrid(PVector loose, float rowSize)
  {
    PVector snapped = inSteps(loose);
    snapped = super.snapToGrid(snapped, rowSize);
    snapped = inMM(snapped);
    return snapped;
  }
  
  public boolean pixelsCanBeExtracted()
  {
    if (super.getImage() == null)
      return false;
    else
      return true;
  }
}

/**
  Polargraph controller
  Copyright Sandy Noble 2012.

  This file is part of Polargraph Controller.

  Polargraph Controller is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Polargraph Controller is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
    
  Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
  Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
  
  This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.

  sandy.noble@gmail.com
  http://www.polargraph.co.uk/
  http://code.google.com/p/polargraph/
*/
/**
*
*
*
*/
class Machine
{
  protected PVector machineSize = new PVector(4000,6000);

  protected Rectangle page = new Rectangle(1000,1000,2000,3000);
  protected Rectangle imageFrame = new Rectangle(1500,1500,1000,1000);
  protected Rectangle pictureFrame = new Rectangle(1600,1600,800,800);

  protected Float stepsPerRev = 800.0f;
  protected Float mmPerRev = 95.0f;
  
  protected Float mmPerStep = null;
  protected Float stepsPerMM = null;
  protected Float maxLength = null;
  protected Float gridSize = 100.0f;
  protected List<Float> gridLinePositions = null;
  
  protected PImage imageBitmap = null;
  protected String imageFilename = null;
  
  
  public Machine(Integer width, Integer height, Float stepsPerRev, Float mmPerRev)
  {
    this.setSize(width, height);
    this.setStepsPerRev(stepsPerRev);
    this.setMMPerRev(mmPerRev);
  }
  
  public void setSize(Integer width, Integer height)
  {
    PVector s = new PVector(width, height);
    this.machineSize = s;
    maxLength = null;
  }
  public PVector getSize()
  {
    return this.machineSize;
  }
  public Float getMaxLength()
  {
    if (maxLength == null)
    {
      maxLength = dist(0,0, getWidth(), getHeight());
    }
    return maxLength;
  }
  
  public void setPage(Rectangle r)
  {
    this.page = r;
  }
  public Rectangle getPage()
  {
    return this.page;
  }
  public float getPageCentrePosition(float pageWidth)
  {
    return (getWidth()- pageWidth/2)/2;
  }

  public void setImageFrame(Rectangle r)
  {
    this.imageFrame = r;
  }

  public Rectangle getImageFrame()
  {
    return this.imageFrame;
  }

  public void setPictureFrame(Rectangle r)
  {  
    this.pictureFrame = r;
  }
  public Rectangle getPictureFrame()
  {
    return this.pictureFrame;
  }
    
  public Integer getWidth()
  {
    return PApplet.parseInt(this.machineSize.x);
  }
  public Integer getHeight()
  {
    return PApplet.parseInt(this.machineSize.y);
  }
  
  public void setStepsPerRev(Float s)
  {
    this.stepsPerRev = s;
  }
  public Float getStepsPerRev()
  {
    mmPerStep = null;
    stepsPerMM = null;
    return this.stepsPerRev;
  }
  public void setMMPerRev(Float d)
  {
    mmPerStep = null;
    stepsPerMM = null;
    this.mmPerRev = d;
  }
  public Float getMMPerRev()
  {
    return this.mmPerRev;
  }
  public Float getMMPerStep()
  {
    if (mmPerStep == null)
    {
      mmPerStep = mmPerRev / stepsPerRev;
    }
    return mmPerStep;
  }
  public Float getStepsPerMM()
  {
    if (stepsPerMM == null)
    {
      stepsPerMM = stepsPerRev / mmPerRev;
    }
    return stepsPerMM;
  }
  
  public int inSteps(int inMM) 
  {
    double steps = inMM * getStepsPerMM();
    steps += 0.5f;
    int stepsInt = (int) steps;
    return stepsInt;
  }
  
  public int inSteps(float inMM) 
  {
    double steps = inMM * getStepsPerMM();
    steps += 0.5f;
    int stepsInt = (int) steps;
    return stepsInt;
  }
  
  public PVector inSteps(PVector mm)
  {
    PVector steps = new PVector(inSteps(mm.x), inSteps(mm.y));
    return steps;
  }
  
  public int inMM(float steps) 
  {
    double mm = steps / getStepsPerMM();
    mm += 0.5f;
    int mmInt = (int) mm;
    return mmInt;
  }
  
  public PVector inMM (PVector steps)
  {
    PVector mm = new PVector(inMM(steps.x), inMM(steps.y));
    return mm;
  }
  
  public float getPixelBrightness(PVector pos, float dim, float scalingFactor)
  {
    float averageBrightness = 255.0f;
    
    if (getImageFrame().surrounds(pos))
    {
      // offset it by image position to get position over image
      PVector offsetPos = PVector.sub(pos, getImageFrame().getPosition());
      int originX = (int) offsetPos.x;
      int originY = (int) offsetPos.y;
      
      PImage extractedPixels = null;

      extractedPixels = getImage().get(PApplet.parseInt(originX*scalingFactor), PApplet.parseInt(originY*scalingFactor), 1, 1);
      extractedPixels.loadPixels();
      
      if (dim >= 2)
      {
        int halfDim = (int)dim / (int)2.0f;
        
        // restrict the sample area from going off the top/left edge of the image
        float startX = originX - halfDim;
        float startY = originY - halfDim;
        
        if (startX < 0)
          startX = 0;
          
        if (startY < 0)
          startY = 0;
  
        // and do the same for the bottom / right edges
        float endX = originX+halfDim;
        float endY = originY+halfDim;
        
        if (endX > getImageFrame().getWidth())
          endX = getImageFrame().getWidth();
          
        if (endY > getImageFrame().getHeight())
          endY = getImageFrame().getHeight();
  
        // now convert end coordinates to width/height
        float dimWidth = (endX - startX)*scalingFactor;
        float dimHeight = (endY - startY)*scalingFactor;
        
        dimWidth = (dimWidth < 1.0f) ? 1.0f : dimWidth;
        dimHeight = (dimHeight < 1.0f) ? 1.0f : dimHeight;
        startX = PApplet.parseInt(startX*scalingFactor);
        startY = PApplet.parseInt(startY*scalingFactor);
        
        // get the block of pixels
        extractedPixels = getImage().get(PApplet.parseInt(startX), PApplet.parseInt(startY), PApplet.parseInt(dimWidth+0.5f), PApplet.parseInt(dimHeight+0.5f));
        extractedPixels.loadPixels();
      }

      // going to go through them and total the brightnesses
      int numberOfPixels = extractedPixels.pixels.length;
      float totalPixelBrightness = 0;
      for (int i = 0; i < numberOfPixels; i++)
      {
        int p = extractedPixels.pixels[i];
        float r = brightness(p);
        totalPixelBrightness += r;
      }
      
      // and get an average brightness for all of these pixels.
      averageBrightness = totalPixelBrightness / numberOfPixels;
    }
    
    return averageBrightness;
  }

  public int getPixelAtMachineCoords(PVector pos, float scalingFactor)
  {
    if (getImageFrame().surrounds(pos))
    {
      // offset it by image position to get position over image
      PVector offsetPos = PVector.sub(pos, getImageFrame().getPosition());
      int originX = (int) offsetPos.x;
      int originY = (int) offsetPos.y;
      
      PImage centrePixel = null;

      centrePixel = getImage().get(PApplet.parseInt(originX*scalingFactor), PApplet.parseInt(originY*scalingFactor), 1, 1);
      centrePixel.loadPixels();
      
      int col = centrePixel.pixels[0];
      return col;
    }
    else 
    {
      return 0;
    }
  }

  public boolean isChromaKey(PVector pos, float scalingFactor)
  {
    if (getImageFrame().surrounds(pos))
    {
      int col = getPixelAtMachineCoords(pos, scalingFactor);

      // get pixels from the vector coords
      if (col == chromaKeyColour)
      {
//        println("is chroma key " + red(col) + ", "+green(col)+","+blue(col));
        return true;
      }
      else
      {
//        println("isn't chroma key " + red(col) + ", "+green(col)+","+blue(col));
        return false;
      }
    }
    else return false;
  }
    
  public PVector asNativeCoords(PVector cartCoords)
  {
    return asNativeCoords(cartCoords.x, cartCoords.y);
  }
  public PVector asNativeCoords(float cartX, float cartY)
  {
    float distA = dist(0,0,cartX, cartY);
    float distB = dist(getWidth(),0,cartX, cartY);
    PVector pgCoords = new PVector(distA, distB);
    return pgCoords;
  }
  
  
  public PVector asCartesianCoords(PVector pgCoords)
  {
    float calcX = PApplet.parseInt((pow(getWidth(), 2) - pow(pgCoords.y, 2) + pow(pgCoords.x, 2)) / (getWidth()*2));
    float calcY = PApplet.parseInt(sqrt(pow(pgCoords.x,2)-pow(calcX,2)));
    PVector vect = new PVector(calcX, calcY);
    return vect;
  }
  
  public Integer convertSizePreset(String preset)
  {
    Integer result = A3_SHORT;
    if (preset.equalsIgnoreCase(PRESET_A3_SHORT))
      result = A3_SHORT;
    else if (preset.equalsIgnoreCase(PRESET_A3_LONG))
      result = A3_LONG;
    else if (preset.equalsIgnoreCase(PRESET_A2_SHORT))
      result = A2_SHORT;
    else if (preset.equalsIgnoreCase(PRESET_A2_LONG))
      result = A2_LONG;
    else if (preset.equalsIgnoreCase(PRESET_A2_IMP_SHORT))
      result = A2_IMP_SHORT;
    else if (preset.equalsIgnoreCase(PRESET_A2_IMP_LONG))
      result = A2_IMP_LONG;
    else if (preset.equalsIgnoreCase(PRESET_A1_SHORT))
      result = A1_SHORT;
    else if (preset.equalsIgnoreCase(PRESET_A1_LONG))
      result = A1_LONG;
    else
    {
      try
      {
        result = Integer.parseInt(preset);
      }
      catch (NumberFormatException nfe)
      {
        result = A3_SHORT;
      }
    }
    return result;
  }
  
  public void loadDefinitionFromProperties(Properties props)
  {
    // get these first because they are important to convert the rest of them
    setStepsPerRev(getFloatProperty("machine.motors.stepsPerRev", 800.0f));
    setMMPerRev(getFloatProperty("machine.motors.mmPerRev", 95.0f));
    
    // now stepsPerMM and mmPerStep should have been calculated. It's safe to get the rest.
   
    // machine size
    setSize(inSteps(getIntProperty("machine.width", 600)), inSteps(getIntProperty("machine.height", 800)));
    
    // page size
    String pageWidth = getStringProperty("controller.page.width", PRESET_A3_SHORT);
    float pw = convertSizePreset(pageWidth);
    String pageHeight = getStringProperty("controller.page.height", PRESET_A3_LONG);
    float ph = convertSizePreset(pageHeight);
    PVector pageSize = new PVector(pw, ph);

    // page position
    String pos = getStringProperty("controller.page.position.x", "CENTRE");
    float px = 0.0f;
    println("machine size: " + getSize().x + ", " + inSteps(pageSize.x));
    if (pos.equalsIgnoreCase("CENTRE"))
    {
      px = inMM((getSize().x - pageSize.x) / 2.0f);
    }
    else
      px = getFloatProperty("controller.page.position.x", (int) getDisplayMachine().getPageCentrePosition(pageSize.x));
      
    float py = getFloatProperty("controller.page.position.y", 120);
      
    PVector pagePos = new PVector(px, py);
    Rectangle page = new Rectangle(inSteps(pagePos), inSteps(pageSize));
    setPage(page);

    // bitmap
    setImageFilename(getStringProperty("controller.image.filename", ""));
    loadImageFromFilename(imageFilename);
  
    // image position
    Float offsetX = getFloatProperty("controller.image.position.x", 0.0f);
    Float offsetY = getFloatProperty("controller.image.position.y", 0.0f);
    PVector imagePos = new PVector(offsetX, offsetY);
//    println("image pos: " + imagePos);
    
    // image size
    Float imageWidth = getFloatProperty("controller.image.width", 500);
    Float imageHeight = getFloatProperty("controller.image.height", 0);
    if (imageHeight == 0) //  default was set
    {
      println("Image height not supplied - creating default.");
      if (getImage() != null)
      {
        float scaling = imageWidth / getImage().width;
        imageHeight = getImage().height * scaling;
      }
      else
        imageHeight = 500.0f;
    }
    PVector imageSize = new PVector(imageWidth, imageHeight);
    
    Rectangle imageFrame = new Rectangle(inSteps(imagePos), inSteps(imageSize));
    setImageFrame(imageFrame);

    // picture frame size
    PVector frameSize = new PVector(getIntProperty("controller.pictureframe.width", 200), getIntProperty("controller.pictureframe.height", 200));
    PVector framePos = new PVector(getIntProperty("controller.pictureframe.position.x", 200), getIntProperty("controller.pictureframe.position.y", 200));
    Rectangle frame = new Rectangle(inSteps(framePos), inSteps(frameSize));
    setPictureFrame(frame);
    
    // penlift positions
    penLiftDownPosition = getIntProperty("machine.penlift.down", 90);
    penLiftUpPosition = getIntProperty("machine.penlift.up", 180);
  }
  
  
  public Properties loadDefinitionIntoProperties(Properties props)
  {
    // Put keys into properties file:
    props.setProperty("machine.motors.stepsPerRev", getStepsPerRev().toString());
    props.setProperty("machine.motors.mmPerRev", getMMPerRev().toString());

    // machine width
    props.setProperty("machine.width", Integer.toString((int) inMM(getWidth())));
    // machine.height
    props.setProperty("machine.height", Integer.toString((int) inMM(getHeight())));

    // image filename
    props.setProperty("controller.image.filename", (getImageFilename() == null) ? "" : getImageFilename());

    // image position
    float imagePosX = 0.0f;
    float imagePosY = 0.0f;
    float imageWidth = 0.0f;
    float imageHeight = 0.0f;
    if (getImageFrame() != null)
    {
      imagePosX = getImageFrame().getLeft();
      imagePosY = getImageFrame().getTop();
      imageWidth = getImageFrame().getWidth();
      imageHeight = getImageFrame().getHeight();
    }
    props.setProperty("controller.image.position.x", Integer.toString((int) inMM(imagePosX)));
    props.setProperty("controller.image.position.y", Integer.toString((int) inMM(imagePosY)));

    // image size
    props.setProperty("controller.image.width", Integer.toString((int) inMM(imageWidth)));
    props.setProperty("controller.image.height", Integer.toString((int) inMM(imageHeight)));

    // page size
    // page position
    float pageSizeX = 0.0f;
    float pageSizeY = 0.0f;
    float pagePosX = 0.0f;
    float pagePosY = 0.0f;
    if (getPage() != null)
    {
      pageSizeX = getPage().getWidth();
      pageSizeY = getPage().getHeight();
      pagePosX = getPage().getLeft();
      pagePosY = getPage().getTop();
    }
    props.setProperty("controller.page.width", Integer.toString((int) inMM(pageSizeX)));
    props.setProperty("controller.page.height", Integer.toString((int) inMM(pageSizeY)));
    props.setProperty("controller.page.position.x", Integer.toString((int) inMM(pagePosX)));
    props.setProperty("controller.page.position.y", Integer.toString((int) inMM(pagePosY)));

    // picture frame size
    float frameSizeX = 0.0f;
    float frameSizeY = 0.0f;
    float framePosX = 0.0f;
    float framePosY = 0.0f;
    if (getPictureFrame() != null)
    {
      frameSizeX = getPictureFrame().getWidth();
      frameSizeY = getPictureFrame().getHeight();
      framePosX = getPictureFrame().getLeft();
      framePosY = getPictureFrame().getTop();
    }
    props.setProperty("controller.pictureframe.width", Integer.toString((int) inMM(frameSizeX)));
    props.setProperty("controller.pictureframe.height", Integer.toString((int) inMM(frameSizeY)));
    
    // picture frame position
    props.setProperty("controller.pictureframe.position.x", Integer.toString((int) inMM(framePosX)));
    props.setProperty("controller.pictureframe.position.y", Integer.toString((int) inMM(framePosY)));
    
    props.setProperty("machine.penlift.down", Integer.toString((int) penLiftDownPosition));
    props.setProperty("machine.penlift.up", Integer.toString((int) penLiftUpPosition));

//    println("framesize: " + inMM(frameSizeX));
    
    return props;
  }

  protected void loadImageFromFilename(String filename)
  {
    if (filename != null && !"".equals(filename))
    {
      // check for format etc here
      println("loading from filename: " + filename);
      try
      {
        this.imageBitmap = loadImage(filename);
        this.imageFilename = filename;
        trace_initTrace(this.imageBitmap);
      }
      catch (Exception e)
      {
        println("Image failed to load: " + e.getMessage());
        this.imageBitmap = null;
      }
      
    }
    else
    {
      this.imageBitmap = null;
      this.imageFilename = null;
    }
  }

  public void sizeImageFrameToImageAspectRatio()
  {
    float scaling = getImageFrame().getWidth() / getImage().width;
    float frameHeight = getImage().height * scaling;
    getImageFrame().getSize().y = frameHeight;
  }
  
  public void setImage(PImage b)
  {
    this.imageBitmap = b;
  }
  public void setImageFilename(String filename)
  {
    this.loadImageFromFilename(filename);
  }
  public String getImageFilename()
  {
    return this.imageFilename;
  }
  public PImage getImage()
  {
    return this.imageBitmap;
  }
  
  public boolean imageIsReady()
  {
    if (imageBitmapIsLoaded())
      return true;
    else
      return false;
  }
  
  public boolean imageBitmapIsLoaded()
  {
    if (getImage() != null)
      return true;
    else
      return false;
  }    

  
  protected void setGridSize(float gridSize)
  {
    this.gridSize = gridSize;
    this.gridLinePositions = generateGridLinePositions(gridSize);
  }
  
  /**
    This takes in an area defined in cartesian steps,
    and returns a set of pixels that are included
    in that area.  Coordinates are specified 
    in cartesian steps.  The pixels are worked out 
    based on the gridsize parameter. d*/
  public Set<PVector> getPixelsPositionsFromArea(PVector p, PVector s, float gridSize, float sampleSize)
  {
    
    // work out the grid
    setGridSize(gridSize);
    float maxLength = getMaxLength();
    float numberOfGridlines = maxLength / gridSize;
    float gridIncrement = maxLength / numberOfGridlines;
    List<Float> gridLinePositions = getGridLinePositions(gridSize);

    Rectangle selectedArea = new Rectangle (p.x,p.y, s.x,s.y);

    // now work out the scaling factor that'll be needed to work out
    // the positions of the pixels on the bitmap.    
    float scalingFactor = getImage().width / getImageFrame().getWidth();
    
    // now go through all the combinations of the two values.
    Set<PVector> nativeCoords = new HashSet<PVector>();
    for (Float a : gridLinePositions)
    {
      for (Float b : gridLinePositions)
      {
        PVector nativeCoord = new PVector(a, b);
        PVector cartesianCoord = asCartesianCoords(nativeCoord);
        if (selectedArea.surrounds(cartesianCoord))
        {
          if (isChromaKey(cartesianCoord, scalingFactor))
          {
            nativeCoord.z = MASKED_PIXEL_BRIGHTNESS; // magic number
            nativeCoords.add(nativeCoord);
          }
          else
          {
            if (sampleSize >= 1.0f)
            {
              float brightness = getPixelBrightness(cartesianCoord, sampleSize, scalingFactor);
              nativeCoord.z = brightness;
            }
            nativeCoords.add(nativeCoord);
          }
        }
      }
    }
    
    return nativeCoords;
  }
  
  protected PVector snapToGrid(PVector loose, float gridSize)
  {
    List<Float> pos = getGridLinePositions(gridSize);
    boolean higherupperFound = false;
    boolean lowerFound = false;
    
    float halfGrid = gridSize / 2.0f;
    float x = loose.x;
    float y = loose.y;
    
    Float snappedX = null;
    Float snappedY = null;
    
    int i = 0;
    while ((snappedX == null || snappedY == null) && i < pos.size())
    {
      float upperBound = pos.get(i)+halfGrid;
      float lowerBound = pos.get(i)-halfGrid;
//      println("pos:" +pos.get(i) + "half: "+halfGrid+ ", upper: "+ upperBound + ", lower: " + lowerBound);
      if (snappedX == null 
        && x > lowerBound 
        && x <= upperBound)
      {
        snappedX = pos.get(i);
//        println("snappedX:" + snappedX);
      }

      if (snappedY == null 
        && y > lowerBound 
        && y <= upperBound)
      {
        snappedY = pos.get(i);
//        println("snappedY:" + snappedY);
      }
            
      i++;
    }
    
    PVector snapped = new PVector((snappedX == null) ? 0.0f : snappedX, (snappedY == null) ? 0.0f : snappedY);
//    println("loose:" + loose);
//    println("snapped:" + snapped);
    return snapped;
  }
  
  protected List<Float> getGridLinePositions(float gridSize)
  {
    setGridSize(gridSize);
    return this.gridLinePositions;
  }
  
  private List<Float> generateGridLinePositions(float gridSize)
  {
    List<Float> glp = new ArrayList<Float>();
    float maxLength = getMaxLength();
    for (float i = gridSize; i <= maxLength; i+=gridSize)
    {
      glp.add(i);
    }
    return glp;
  }

 

}
/**
  Polargraph controller
  Copyright Sandy Noble 2012.

  This file is part of Polargraph Controller.

  Polargraph Controller is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Polargraph Controller is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
    
  Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
  Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
  
  This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.

  sandy.noble@gmail.com
  http://www.polargraph.co.uk/
  http://code.google.com/p/polargraph/
*/

class Scaler
{
  public float scale = 1.0f;
  public float mmPerStep = 1.0f;
  
  public Scaler(float scale, float mmPerStep)
  {
    this.scale = scale;
    this.mmPerStep = mmPerStep;
  }
  public void setScale(float scale)
  {
    this.scale = scale;
  }
  
  public float scale(float in)
  {
    return in * mmPerStep * scale;
  }
}

class PreviewVector extends PVector
{
  public String command;
}






public class Console extends WindowAdapter implements WindowListener, ActionListener, Runnable
{
  private JFrame frame;
  private JTextArea textArea;
  private Thread reader;
  private Thread reader2;
  private boolean quit;

  private final PipedInputStream pin=new PipedInputStream(); 
  private final PipedInputStream pin2=new PipedInputStream(); 

  private PrintStream cOut = System.out;
  private PrintStream cErr = System.err;

  Thread errorThrower; // just for testing (Throws an Exception at this Console

  public Console()
  {
    // create all components and add them
    frame=new JFrame("Java Console");
    Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize=new Dimension((int)(screenSize.width/2),(int)(screenSize.height/2));
    int x=(int)(frameSize.width/2);
    int y=(int)(frameSize.height/2);
    frame.setBounds(x,y,frameSize.width,frameSize.height);

    textArea=new JTextArea();
    textArea.setEditable(false);
    JButton button=new JButton("clear");

    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(new JScrollPane(textArea),BorderLayout.CENTER);
    frame.getContentPane().add(button,BorderLayout.SOUTH);
    frame.setVisible(true);		

    frame.addWindowListener(this);		
    button.addActionListener(this);

    try
    {
      this.cOut = System.out;
      PipedOutputStream pout=new PipedOutputStream(this.pin);
      System.setOut(new PrintStream(pout,true)); 
    } 
    catch (java.io.IOException io)
    {
      textArea.append("Couldn't redirect STDOUT to this console\n"+io.getMessage());
    }
    catch (SecurityException se)
    {
      textArea.append("Couldn't redirect STDOUT to this console\n"+se.getMessage());
    } 

    try 
    {
      this.cErr = System.err;
      PipedOutputStream pout2=new PipedOutputStream(this.pin2);
      System.setErr(new PrintStream(pout2,true));
    } 
    catch (java.io.IOException io)
    {
      textArea.append("Couldn't redirect STDERR to this console\n"+io.getMessage());
    }
    catch (SecurityException se)
    {
      textArea.append("Couldn't redirect STDERR to this console\n"+se.getMessage());
    } 		

    quit=false; // signals the Threads that they should exit

    // Starting two seperate threads to read from the PipedInputStreams				
    //
    reader=new Thread(this);
    reader.setDaemon(true);	
    reader.start();	
    //
    reader2=new Thread(this);	
    reader2.setDaemon(true);	
    reader2.start();

//    // testing part
//    // you may omit this part for your application
//    // 
//    System.out.println("Hello World 2");
//    System.out.println("All fonts available to Graphic2D:\n");
//    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//    String[] fontNames=ge.getAvailableFontFamilyNames();
//    for(int n=0;n<fontNames.length;n++)  System.out.println(fontNames[n]);		
//    // Testing part: simple an error thrown anywhere in this JVM will be printed on the Console
//    // We do it with a seperate Thread becasue we don't wan't to break a Thread used by the Console.
//    System.out.println("\nLets throw an error on this console");	
//    errorThrower=new Thread(this);
//    errorThrower.setDaemon(true);
//    errorThrower.start();					
  }

  public synchronized void windowClosed(WindowEvent evt)
  {
    quit=true;
    this.notifyAll(); // stop all threads
    try { 
      reader.join(1000);
      pin.close();
      System.setOut(this.cOut);
    } 
    catch (Exception e){
    }		
    try { 
      reader2.join(1000);
      pin2.close(); 
      System.setErr(this.cErr);
    } 
    catch (Exception e){
    }
//    System.exit(0);
  }		

  public synchronized void windowClosing(WindowEvent evt)
  {
    frame.setVisible(false); // default behaviour of JFrame	
    frame.dispose();
  }

  public synchronized void actionPerformed(ActionEvent evt)
  {
    textArea.setText("");
  }

  public synchronized void run()
  {
    try
    {			
      while (Thread.currentThread()==reader)
      {
        try { 
          this.wait(100);
        }
        catch(InterruptedException ie) {
        }
        if (pin.available()!=0)
        {
          String input=this.readLine(pin);
          textArea.append(input);
          textArea.setCaretPosition(textArea.getDocument().getLength());

        }
        if (quit) return;
      }

      while (Thread.currentThread()==reader2)
      {
        try { 
          this.wait(100);
        }
        catch(InterruptedException ie) {
        }
        if (pin2.available()!=0)
        {
          String input=this.readLine(pin2);
          textArea.append(input);
          textArea.setCaretPosition(textArea.getDocument().getLength());

        }
        if (quit) return;
      }			
    } 
    catch (Exception e)
    {
      textArea.append("\nConsole reports an Internal error.");
      textArea.append("The error is: "+e);			
    }
  }
  
  public void close()
  {
    this.windowClosing(null);
  }

  public synchronized String readLine(PipedInputStream in) throws IOException
  {
    String input="";
    do
    {
      int available=in.available();
      if (available==0) break;
      byte b[]=new byte[available];
      in.read(b);
      input=input+new String(b,0,b.length);														
    }
    while( !input.endsWith("\n") &&  !input.endsWith("\r\n") && !quit);
    return input;
  }				
}
/**
  Polargraph controller
  Copyright Sandy Noble 2012.

  This file is part of Polargraph Controller.

  Polargraph Controller is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Polargraph Controller is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
    
  Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
  Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
  
  This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.

  sandy.noble@gmail.com
  http://www.polargraph.co.uk/
  http://code.google.com/p/polargraph/
*/
class Panel
{
  private Rectangle outline = null;
  private String name = null;
  private List<Controller> controls = null;
  private Map<String, PVector> controlPositions = null;
  private Map<String, PVector> controlSizes = null;
  private boolean resizable = true;
  private float minimumHeight = DEFAULT_CONTROL_SIZE.y+4;
  private int outlineColour = color(255);
  
  public final int CONTROL_COL_BG_DEFAULT = color(0,54,82);
  public final int CONTROL_COL_BG_DISABLED = color(20,44,62);
  public final int CONTROL_COL_LABEL_DEFAULT = color(255);
  public final int CONTROL_COL_LABEL_DISABLED = color(200);

  public Panel(String name, Rectangle outline)
  {
    this.name = name;
    this.outline = outline;
  }
  
  public Rectangle getOutline()
  {
    return this.outline;
  }
  public void setOutline(Rectangle r)
  {
    this.outline = r;
  }
  
  public String getName()
  {
    return this.name;
  }
  public void setName(String name)
  {
    this.name = name;
  }
  
  public List<Controller> getControls()
  {
    if (this.controls == null)
      this.controls = new ArrayList<Controller>(0);
    return this.controls;
  }
  public void setControls(List<Controller> c)
  {
    this.controls = c;
  }
  
  public Map<String, PVector> getControlPositions()
  {
    return this.controlPositions;
  }
  public void setControlPositions(Map<String, PVector> cp)
  {
    this.controlPositions = cp;
  }
  
  public Map<String, PVector> getControlSizes()
  {
    return this.controlSizes;
  }
  public void setControlSizes(Map<String, PVector> cs)
  {
    this.controlSizes = cs;
  }
  
  public void setOutlineColour(int c)
  {
    this.outlineColour = c;
  }
  
  public void setResizable(boolean r)
  {
    this.resizable = r;
  }
  public boolean isResizable()
  {
    return this.resizable;
  }
  
  public void setMinimumHeight(float h)
  {
    this.minimumHeight = h;
  }
  public float getMinimumHeight()
  {
    return this.minimumHeight;
  }
  
  public void draw()
  {
//    stroke(outlineColour);
//    strokeWeight(2);
//    rect(getOutline().getLeft(), getOutline().getTop(), getOutline().getWidth(), getOutline().getHeight());

    drawControls();
  }
  
  public void drawControls()
  {
    for (Controller c : this.getControls())
    {
      //println("Control: " + c.name());
      PVector pos = getControlPositions().get(c.name());
      float x = pos.x+getOutline().getLeft();
      float y = pos.y+getOutline().getTop();
      c.setPosition(x, y);
      
      PVector cSize = getControlSizes().get(c.name());
      c.setSize((int)cSize.x, (int)cSize.y);

      boolean locked = false;
      
      // theres a few cases here where the controls are locked (disabled)
      
      // any drawing / extracting controls are disabled if there is no selec
      // box specified.
      if (getControlsToLockIfBoxNotSpecified().contains(c.name()) && !isBoxSpecified())
      {
        locked = true;        
      }
      
      // if there is no vector shape loaded then lock the "draw vector"
      // control.
      if (c.name().equals(MODE_RENDER_VECTORS) && getVectorShape() == null)
      {
        locked = true;
      }
  
      // if there's no image loaded, then hide resizing/moving
      if (getControlsToLockIfImageNotLoaded().contains(c.name()) && getDisplayMachine().getImage() == null)
      {
        locked = true;        
      }
      
      if (c.name().equals(MODE_LOAD_VECTOR_FILE))
      {
        if (getVectorShape() != null)
          c.setLabel("Clear vector");
        else
          c.setLabel("Load vector");
      }
      else if (c.name().equals(MODE_LOAD_IMAGE))
      {
        if (getDisplayMachine().getImage() != null)
          c.setLabel("Clear image");
        else
          c.setLabel("Load image file");
      }
      

      int col = c.getColor().getBackground();      
      setLock(c, locked);
    }
  }
  
  public void setLock(Controller c, boolean locked) 
  {
    c.setLock(locked);
    if (locked) 
    {
      c.setColorBackground(CONTROL_COL_BG_DISABLED);
      c.setColorLabel(CONTROL_COL_LABEL_DISABLED);
    } 
    else 
    {
      c.setColorBackground(CONTROL_COL_BG_DEFAULT);
      c.setColorLabel(CONTROL_COL_LABEL_DEFAULT);
    }
  }  
  
  public void setHeight(float h)
  {
    if (this.isResizable())
    {
      if (h <= getMinimumHeight())
        this.getOutline().setHeight(getMinimumHeight());
      else
        this.getOutline().setHeight(h);
      setControlPositions(buildControlPositionsForPanel(this));
      
      float left = 0.0f;
      String controlName = "";
      for (String key : getControlPositions().keySet())
      {
        PVector pos = getControlPositions().get(key);
        if (pos.x >= left)
        {
          left = pos.x;
          controlName = key;
        }
      }
      
      Map<String, PVector> map = getControlSizes();
      
//      PVector size = getControlSizes().get(controlName);
//      println("size: " + size);
      float right = left + DEFAULT_CONTROL_SIZE.x;
      
      this.getOutline().setWidth(right);
    }
  }
  
  
}
/**
  Polargraph controller
  Copyright Sandy Noble 2012.

  This file is part of Polargraph Controller.

  Polargraph Controller is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Polargraph Controller is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
    
  Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
  Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
  
  This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.

  sandy.noble@gmail.com
  http://www.polargraph.co.uk/
  http://code.google.com/p/polargraph/
*/
class Rectangle
{
  public PVector position = null;
  public PVector size = null;
  
  public Rectangle(float px, float py, float width, float height)
  {
    this.position = new PVector(px, py);
    this.size = new PVector(width, height);
  }
  public Rectangle(PVector position, PVector size)
  {
    this.position = position;
    this.size = size;
  }
  public Rectangle(Rectangle r)
  {
    this.position = new PVector(r.getPosition().x, r.getPosition().y);
    this.size = new PVector(r.getSize().x, r.getSize().y);
  }
  
  public float getWidth()
  {
    return this.size.x;
  }
  public void setWidth(float w)
  {
    this.size.x = w;
  }
  public float getHeight()
  {
    return this.size.y;
  }
  public void setHeight(float h)
  {
    this.size.y = h;
  }
  public PVector getPosition()
  {
    return this.position;
  }
  public PVector getSize()
  {
    return this.size;
  }
  public PVector getTopLeft()
  {
    return getPosition();
  }
  public PVector getBotRight()
  {
    return PVector.add(this.position, this.size);
  }
  public float getLeft()
  {
    return getPosition().x;
  }
  public float getRight()
  {
    return getPosition().x + getSize().x;
  }
  public float getTop()
  {
    return getPosition().y;
  }
  public float getBottom()
  {
    return getPosition().y + getSize().y;
  }
  
  public void setPosition(float x, float y)
  {
    if (this.position == null)
      this.position = new PVector(x, y);
    else
    {
      this.position.x = x;
      this.position.y = y;
    }
  }
  
  public Boolean surrounds(PVector p)
  {
    if (p.x >= this.getLeft()
    && p.x < this.getRight()
    && p.y >= this.getTop()
    && p.y < this.getBottom()-1)
      return true;
    else
      return false;
  }
  
}
/**
  Polargraph controller
  Copyright Sandy Noble 2012.

  This file is part of Polargraph Controller.

  Polargraph Controller is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Polargraph Controller is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
    
  Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
  Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
  
  This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.

  sandy.noble@gmail.com
  http://www.polargraph.co.uk/
  http://code.google.com/p/polargraph/
*/
public void button_mode_begin()
{
  button_mode_clearQueue();
}
public void numberbox_mode_changeGridSize(float value)
{
  setGridSize(value);
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
  }
}
public void numberbox_mode_changeSampleArea(float value)
{
  setSampleArea(value);
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
  }
}
public void numberbox_mode_changePixelScaling(float value)
{
  setPixelScalingOverGridSize(value);
}
public void minitoggle_mode_showImage(boolean flag)
{
  this.displayingImage = flag;
}
public void minitoggle_mode_showVector(boolean flag)
{
  this.displayingVector = flag;
}
public void minitoggle_mode_showDensityPreview(boolean flag)
{
  this.displayingDensityPreview = flag;
}
public void minitoggle_mode_showQueuePreview(boolean flag)
{
  this.displayingQueuePreview = flag;
}
public void minitoggle_mode_showGuides(boolean flag)
{
  this.displayingGuides = flag;
}
public void unsetOtherToggles(String except)
{
  for (String name : getAllControls().keySet())
  {
    if (name.startsWith("toggle_"))
    {
      if (name.equals(except))
      {
//        println("not resetting this one.");
      }
      else
      {
        getAllControls().get(name).setValue(0);
      }
    }
  }
}
public void button_mode_penUp()
{
  addToCommandQueue(CMD_PENUP + penLiftUpPosition +",END");
}
public void button_mode_penDown()
{
  addToCommandQueue(CMD_PENDOWN + penLiftDownPosition +",END");
}
public void numberbox_mode_penUpPos(int value)
{
  penLiftUpPosition =  value;
}
public void numberbox_mode_penDownPos(int value)
{
  penLiftDownPosition =  value;
}
public void button_mode_sendPenliftRange()
{
  addToCommandQueue(CMD_SETPENLIFTRANGE+penLiftDownPosition+","+penLiftUpPosition+",END");
}  
public void button_mode_sendPenliftRangePersist()
{
  addToCommandQueue(CMD_SETPENLIFTRANGE+penLiftDownPosition+","+penLiftUpPosition+",1,END");
}  

public void numberbox_mode_liveBlurValue(int value)
{
  if (value != blurValue)
  {
    blurValue =  value;
    retraceShape = true;
  }
}
public void numberbox_mode_liveSimplificationValue(int value)
{
  if (value != liveSimplification)
  {
    liveSimplification =  value;
    retraceShape = true;
  }
}
public void numberbox_mode_livePosteriseValue(int value)
{
  if (value != posterizeValue)
  {
    posterizeValue =  value;
    retraceShape = true;
  }
}
public void button_mode_liveCaptureFromLive()
{
  trace_captureCurrentImage();
}
public void button_mode_liveClearCapture()
{
  captureShape = null;
}
public void button_mode_liveAddCaption()
{
  
}
public void numberbox_mode_vectorPathLengthHighPassCutoff(int value)
{
  pathLengthHighPassCutoff =  value;
}

public void button_mode_liveConfirmDraw()
{
  if (captureShape != null)
  {
    confirmedDraw = true;
    
    // work out scaling and position
    float scaling = getDisplayMachine().inMM(getDisplayMachine().getImageFrame().getWidth()) / captureShape.getWidth();
    PVector position = new PVector(getDisplayMachine().inMM(getDisplayMachine().getImageFrame().getPosition().x), 
    getDisplayMachine().inMM(getDisplayMachine().getImageFrame().getPosition().y));
  
    sendVectorShapes(captureShape, scaling, position, PATH_SORT_CENTRE_FIRST);
    button_mode_penUp();

    //  save shape as SVG
    trace_saveShape(captureShape);
  }
} 
public void toggle_mode_showWebcamRawVideo(boolean flag)
{
//  drawingLiveVideo = flag;
}
public void toggle_mode_flipWebcam(boolean flag)
{
  flipWebcamImage = flag;
}
public void toggle_mode_rotateWebcam(boolean flag)
{
  rotateWebcamImage = flag;
}


public void toggle_mode_inputBoxTopLeft(boolean flag)
{
  if (flag)
  {
    unsetOtherToggles(MODE_INPUT_BOX_TOP_LEFT);
    setMode(MODE_INPUT_BOX_TOP_LEFT);
  }
  else
    currentMode = "";
}
public void toggle_mode_inputBoxBotRight(boolean flag)
{
  if (flag)
  {
    unsetOtherToggles(MODE_INPUT_BOX_BOT_RIGHT);
    setMode(MODE_INPUT_BOX_BOT_RIGHT);
    // unset topleft
  }
  else
    currentMode = "";
}
public void button_mode_drawOutlineBox()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
    sendOutlineOfBox();
}
public void button_mode_drawOutlineBoxRows()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    // get the pixels
    Set<PVector> pixels = getDisplayMachine().extractNativePixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    sendOutlineOfRows(pixels, DRAW_DIR_SE);
  }
}
public void button_mode_drawShadeBoxRowsPixels()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    // get the pixels
    Set<PVector> pixels = getDisplayMachine().extractNativePixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    sendOutlineOfPixels(pixels);
  }
}
public void toggle_mode_drawToPosition(boolean flag)
{
  // unset other toggles
  if (flag)
  {
    unsetOtherToggles(MODE_DRAW_TO_POSITION);
    setMode(MODE_DRAW_TO_POSITION);
  }
}
public void button_mode_renderSquarePixel()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    // get the pixels
    Set<PVector> pixels = getDisplayMachine().extractNativePixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    sendSquarePixels(pixels);
  }
}
public void button_mode_renderSawPixel()
{
//  if (pixelCentresForMachine != null && !pixelCentresForMachine.isEmpty())
//    sendSawtoothPixels();
}
public void button_mode_renderCirclePixel()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    Set<PVector> pixels = getDisplayMachine().extractNativePixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    sendCircularPixels(pixels);
  }
}
public void button_mode_renderVectors()
{
  // turn off vector view and turn queue preview on
  //minitoggle_mode_showVector(false);
  minitoggle_mode_showQueuePreview(true);
  println("here");
  sendVectorShapes();
}

public void toggle_mode_setPosition(boolean flag)
{
  if (flag)
  {
    unsetOtherToggles(MODE_SET_POSITION);
    setMode(MODE_SET_POSITION);
  }
}

public void button_mode_returnToHome()
{
  // lift pen
  button_mode_penUp();
  PVector pgCoords = getDisplayMachine().asNativeCoords(getHomePoint());
  sendMoveToNativePosition(false, pgCoords);
}

public void button_mode_drawTestPattern()
{
  sendTestPattern();
}

public void button_mode_drawGrid()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    Set<PVector> pixels = getDisplayMachine().extractNativePixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    sendGridOfBox(pixels);
  }
}
public void button_mode_loadImage()
{
  if (getDisplayMachine().getImage() == null)
  {
    loadImageWithFileChooser();
    if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
    {
      getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    }
  }
  else
  {
    getDisplayMachine().setImage(null);
    getDisplayMachine().setImageFilename(null);
  }
}
public void button_mode_loadVectorFile()
{
  if (getVectorShape() == null)
  {
    loadVectorWithFileChooser();
    minitoggle_mode_showVector(true);
  }
  else
  {
    vectorShape = null;
    vectorFilename = null;
  }
}
public void numberbox_mode_pixelBrightThreshold(float value)
{
  pixelExtractBrightThreshold = PApplet.parseInt(value+0.5f);
}
public void numberbox_mode_pixelDarkThreshold(float value)
{
  pixelExtractDarkThreshold = PApplet.parseInt(value+0.5f);
}

public void button_mode_pauseQueue()
{
}
public void button_mode_runQueue()
{
}
public void button_mode_clearQueue()
{
  resetQueue();
}
public void button_mode_setPositionHome()
{
  sendSetHomePosition();
}
public void button_mode_drawTestPenWidth()
{
  sendTestPenWidth();
}
public void button_mode_renderScaledSquarePixels()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    // get the pixels
    Set<PVector> pixels = getDisplayMachine().extractNativePixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    sendScaledSquarePixels(pixels);
  }
}
public void button_mode_renderSolidSquarePixels()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    // get the pixels
    Set<PVector> pixels = getDisplayMachine().extractNativePixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    sendSolidSquarePixels(pixels);
  }
}
public void button_mode_renderScribblePixels()
{
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
  {
    // get the pixels
    Set<PVector> pixels = getDisplayMachine().extractNativePixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), sampleArea);
    sendScribblePixels(pixels);
  }
}
public void button_mode_changeMachineSpec()
{
  sendMachineSpec();
}
public void button_mode_requestMachineSize()
{
  sendRequestMachineSize();
}
public void button_mode_resetMachine()
{
  sendResetMachine();
}
public void button_mode_saveProperties()
{
  savePropertiesFile();
  // clear old properties.
  props = null;
  loadFromPropertiesFile();
}
public void button_mode_saveAsProperties()
{
  saveNewPropertiesFileWithFileChooser();
}
public void button_mode_loadProperties()
{
  loadNewPropertiesFilenameWithFileChooser();
}
public void toggle_mode_moveImage(boolean flag)
{
  if (flag)
  {
    unsetOtherToggles(MODE_MOVE_IMAGE);
    setMode(MODE_MOVE_IMAGE);
  }
  else
  {
    setMode("");
  }
}

public void toggle_mode_chooseChromaKeyColour(boolean flag)
{
  if (flag)
  {
    unsetOtherToggles(MODE_CHOOSE_CHROMA_KEY_COLOUR);
    setMode(MODE_CHOOSE_CHROMA_KEY_COLOUR);
  }
  else
    setMode("");
}

public void button_mode_convertBoxToPictureframe()
{
  setPictureFrameDimensionsToBox();
}
public void button_mode_selectPictureframe()
{
  setBoxToPictureframeDimensions();
}
public void button_mode_exportQueue()
{
  exportQueueToFile();
}
public void button_mode_importQueue()
{
  importQueueFromFile();
}
public void toggle_mode_drawDirect(boolean flag)
{
  if (flag)
  {
    unsetOtherToggles(MODE_DRAW_DIRECT);
    setMode(MODE_DRAW_DIRECT);
  }
}

public void numberbox_mode_resizeImage(float value)
{
  float steps = getDisplayMachine().inSteps(value);
  Rectangle r = getDisplayMachine().getImageFrame();
  float ratio = r.getHeight() / r.getWidth();

  float oldSize = r.getSize().x;
  
  r.getSize().x = steps;
  r.getSize().y = steps * ratio;

  float difference = (r.getSize().x / 2.0f)-(oldSize/2.0f);
  r.getPosition().x -= difference;
  r.getPosition().y -= difference * ratio;
  
  if (getDisplayMachine().pixelsCanBeExtracted() && isBoxSpecified())
    getDisplayMachine().extractPixelsFromArea(getBoxVector1(), getBoxVectorSize(), getGridSize(), getSampleArea());
}

public void numberbox_mode_resizeVector(float value)
{
  if (getVectorShape() != null)
  {
    // get current size of vector in local coordinates
    PVector oldVectorSize = new PVector(getVectorShape().width, getVectorShape().height);
    oldVectorSize = PVector.mult(oldVectorSize, (vectorScaling/100));
    // and current centre point of vector
    PVector oldCentroid = new PVector(oldVectorSize.x / 2.0f, oldVectorSize.y / 2.0f);
    
    // get newly scaled size of vector
    PVector newVectorSize = new PVector(getVectorShape().width, getVectorShape().height);
    newVectorSize = PVector.mult(newVectorSize, (value/100));
    // and new centre point of vector
    PVector newCentroid = new PVector(newVectorSize.x / 2.0f, newVectorSize.y / 2.0f);
    
    // difference is current centre minus new centre
    PVector difference = PVector.sub(oldCentroid, newCentroid);
    
    // add difference onto vector position
    PVector newVectorPosition = PVector.add(vectorPosition, difference);
    vectorPosition = newVectorPosition;
  }
  
  vectorScaling = value;
  
}
public void toggle_mode_moveVector(boolean flag)
{
  // unset other toggles
  if (flag)
  {
    unsetOtherToggles(MODE_MOVE_VECTOR);
    setMode(MODE_MOVE_VECTOR);
  }
  else
  {
    setMode("");
  }
}

public void numberbox_mode_changeMachineWidth(float value)
{
  clearBoxVectors();
  float steps = getDisplayMachine().inSteps(value);
  getDisplayMachine().getSize().x = steps;
}
public void numberbox_mode_changeMachineHeight(float value)
{
  clearBoxVectors();
  float steps = getDisplayMachine().inSteps(value);
  getDisplayMachine().getSize().y = steps;
}
public void numberbox_mode_changeMMPerRev(float value)
{
  clearBoxVectors();
  getDisplayMachine().setMMPerRev(value);
}
public void numberbox_mode_changeStepsPerRev(float value)
{
  clearBoxVectors();
  getDisplayMachine().setStepsPerRev(value);
}
public void numberbox_mode_changeStepMultiplier(float value)
{
  machineStepMultiplier = (int) value;
}
public void numberbox_mode_changeMinVectorLineLength(float value)
{
  minimumVectorLineLength = (int) value;
}
public void numberbox_mode_changePageWidth(float value)
{
  float steps = getDisplayMachine().inSteps(value);
  getDisplayMachine().getPage().setWidth(steps);
}
public void numberbox_mode_changePageHeight(float value)
{
  float steps = getDisplayMachine().inSteps(value);
  getDisplayMachine().getPage().setHeight(steps);
}
public void numberbox_mode_changePageOffsetX(float value)
{
  float steps = getDisplayMachine().inSteps(value);
  getDisplayMachine().getPage().getTopLeft().x = steps;
}
public void numberbox_mode_changePageOffsetY(float value)
{
  float steps = getDisplayMachine().inSteps(value);
  getDisplayMachine().getPage().getTopLeft().y = steps;
}
public void button_mode_changePageOffsetXCentre()
{
  float pageWidth = getDisplayMachine().getPage().getWidth();
  float machineWidth = getDisplayMachine().getSize().x;
  float diff = (machineWidth - pageWidth) / 2.0f;
  getDisplayMachine().getPage().getTopLeft().x = diff;
  initialiseNumberboxValues(getAllControls());
}

public void numberbox_mode_changeHomePointX(float value)
{
  float steps = getDisplayMachine().inSteps(value);
  getHomePoint().x = steps;
}
public void numberbox_mode_changeHomePointY(float value)
{
  float steps = getDisplayMachine().inSteps(value);
  getHomePoint().y = steps;
}
public void button_mode_changeHomePointXCentre()
{
  float halfWay = getDisplayMachine().getSize().x / 2.0f;
  getHomePoint().x = halfWay;
  getHomePoint().y = getDisplayMachine().getPage().getTop();
  initialiseNumberboxValues(getAllControls());
}


public void numberbox_mode_changePenWidth(float value)
{
  currentPenWidth =  Math.round(value*100.0f)/100.0f;
}
public void button_mode_sendPenWidth()
{
  NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
  DecimalFormat df = (DecimalFormat)nf;  
  df.applyPattern("###.##");
  addToRealtimeCommandQueue(CMD_CHANGEPENWIDTH+df.format(currentPenWidth)+",END");
}  

public void numberbox_mode_changePenTestStartWidth(float value)
{
  testPenWidthStartSize = Math.round(value*100.0f)/100.0f;
}
public void numberbox_mode_changePenTestEndWidth(float value)
{
  testPenWidthEndSize = Math.round(value*100.0f)/100.0f;
}
public void numberbox_mode_changePenTestIncrementSize(float value)
{
  testPenWidthIncrementSize = Math.round(value*100.0f)/100.0f;
}

public void numberbox_mode_changeMachineMaxSpeed(float value)
{
  currentMachineMaxSpeed =  Math.round(value*100.0f)/100.0f;
}
public void numberbox_mode_changeMachineAcceleration(float value)
{
  currentMachineAccel =  Math.round(value*100.0f)/100.0f;
}
public void button_mode_sendMachineSpeed()
{
  NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
  DecimalFormat df = (DecimalFormat)nf;  

  df.applyPattern("###.##");
  addToRealtimeCommandQueue(CMD_SETMOTORSPEED+df.format(currentMachineMaxSpeed)+",END");

  df.applyPattern("###.##");
  addToRealtimeCommandQueue(CMD_SETMOTORACCEL+df.format(currentMachineAccel)+",END");
}

public void button_mode_sendMachineSpeedPersist()
{
  NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
  DecimalFormat df = (DecimalFormat)nf;  

  df.applyPattern("###.##");
  addToCommandQueue(CMD_SETMOTORSPEED+df.format(currentMachineMaxSpeed)+",1,END");

  df.applyPattern("###.##");
  addToCommandQueue(CMD_SETMOTORACCEL+df.format(currentMachineAccel)+",1,END");
}

public void button_mode_sendRoveArea()
{
  if (isBoxSpecified())
  {
    addToCommandQueue(CMD_SET_ROVE_AREA+(long)boxVector1.x+","+(long)boxVector1.y+","
    +(long)(boxVector2.x-boxVector1.x)+","+(long)(boxVector2.y-boxVector1.y)+",END");
  }
}

public void button_mode_selectRoveImageSource()
{
  addToCommandQueue(CMD_SELECT_ROVE_SOURCE_IMAGE+",w1.pbm,END");
}
public void button_mode_startMarking()
{
  // C47,<start (1) or stop (0)>,<mark style>,END
  addToCommandQueue(CMD_RENDER_ROVE+",1,1,END");
}
public void button_mode_stopMarking()
{
  addToCommandQueue(CMD_RENDER_ROVE+",0,0,END");
}

public void toggle_mode_sendStartText(boolean flag)
{
  if (flag)
  {
    unsetOtherToggles(MODE_SEND_START_TEXT);
    setMode(MODE_SEND_START_TEXT);
  }
  else
  {
    setMode("");
  }
}

public void button_mode_startSwirling()
{
  addToCommandQueue(CMD_SWIRLING+"1,END");
}
public void button_mode_stopSwirling()
{
  addToCommandQueue(CMD_SWIRLING+"0,END");
}
public void setMode(String m)
{
  lastMode = currentMode;
  currentMode = m;
}
public void revertToLastMode()
{
  currentMode = lastMode;
}

/*------------------------------------------------------------------------
    Details about the "serial port" subwindow
------------------------------------------------------------------------*/

public void button_mode_serialPortDialog()
{
  ControlWindow serialPortWindow = cp5.addControlWindow("changeSerialPortWindow",100,100,150,150);
  serialPortWindow.hideCoordinates();
  
  serialPortWindow.setBackground(getBackgroundColour());
  Radio r = cp5.addRadio("radio_serialPort",10,10);
  r.setWindow(serialPortWindow);

  String[] ports = Serial.list();
  if (getSerialPortNumber() >= 0 && getSerialPortNumber() < ports.length)
    r.setValue(getSerialPortNumber());
    
  r.add("setup", -2);
  r.add("No serial connection", -1);
  
  for (int i = 0; i < ports.length; i++)
  {
    r.add(ports[i], i);
  }
  
  int portNo = getSerialPortNumber();
  if (portNo > -1 && portNo < ports.length)
    r.activate(ports[portNo]);
  else
    r.activate("No serial connection");
    
  r.removeItem("setup");
}

public void radio_serialPort(int newSerialPort) 
{
  if (newSerialPort == -2)
  {
  }
  else if (newSerialPort == -1)
  {
    println("Disconnecting serial port.");
    useSerialPortConnection = false;
    if (myPort != null)
    {
      myPort.stop();
      myPort = null;
    }
    drawbotReady = false;
    drawbotConnected = false;
    serialPortNumber = newSerialPort;
  }
  else if (newSerialPort != getSerialPortNumber())
  {
    println("About to connect to serial port in slot " + newSerialPort);
    // Print a list of the serial ports, for debugging purposes:
    if (newSerialPort < Serial.list().length)
    {
      try 
      {
        drawbotReady = false;
        drawbotConnected = false;
        if (myPort != null)
        {
          myPort.stop();
          myPort = null;
        }
        if (getSerialPortNumber() >= 0)
          println("closing " + Serial.list()[getSerialPortNumber()]);
        
        serialPortNumber = newSerialPort;
        String portName = Serial.list()[serialPortNumber];
  
        myPort = new Serial(this, portName, getBaudRate());
        //read bytes into a buffer until you get a linefeed (ASCII 10):
        myPort.bufferUntil('\n');
        useSerialPortConnection = true;
        println("Successfully connected to port " + portName);
      }
      catch (Exception e)
      {
        println("Attempting to connect to serial port in slot " + getSerialPortNumber() 
        + " caused an exception: " + e.getMessage());
      }
    }
    else
    {
      println("No serial ports found.");
      useSerialPortConnection = false;
    }
  }
  else
  {
    println("no serial port change.");
  }
}


/*------------------------------------------------------------------------
    Details about the "machine store" subwindow
------------------------------------------------------------------------*/

ControlWindow dialogWindow = null;

public void button_mode_machineStoreDialog()
{
  this.dialogWindow = cp5.addControlWindow("chooseStoreFilenameWindow",100,100,450,150);
  dialogWindow.hideCoordinates();
  
  dialogWindow.setBackground(getBackgroundColour());

  Textfield filenameField = cp5.addTextfield("storeFilename",20,20,150,20);
  filenameField.setText(getStoreFilename());
  filenameField.setLabel("Filename to store to");
  filenameField.setWindow(dialogWindow);

  Button submitButton = cp5.addButton("submitStoreFilenameWindow",0,180,20,60,20);
  submitButton.setLabel("Submit");
  submitButton.setWindow(dialogWindow);

  Toggle overwriteToggle = cp5.addToggle("toggleAppendToFile",true,180,50,20,20);
  overwriteToggle.setCaptionLabel("Overwrite existing file");
  overwriteToggle.setWindow(dialogWindow);

  filenameField.setFocus(true);

}

public void storeFilename(String filename)
{
  println("Filename event: "+ filename);
  if (filename != null && filename.length() <= 12)
  {
    setStoreFilename(filename);
    sendMachineStoreMode();
  }
}

public void toggleAppendToFile(boolean theFlag) 
{
  setOverwriteExistingStoreFile(theFlag);
}

public void submitStoreFilenameWindow(int theValue) 
{
  Textfield tf = (Textfield) cp5.controller("storeFilename");
  tf.submit();
}

public void button_mode_machineExecDialog()
{
  this.dialogWindow = cp5.addControlWindow("chooseExecFilenameWindow",100,100,450,150);
  dialogWindow.hideCoordinates();
  
  dialogWindow.setBackground(getBackgroundColour());

  Textfield filenameField = cp5.addTextfield("execFilename",20,20,150,20);
  filenameField.setText(getStoreFilename());
  filenameField.setLabel("Filename to execute from");
  filenameField.setWindow(dialogWindow);

  Button submitButton = cp5.addButton("submitExecFilenameWindow",0,180,20,60,20);
  submitButton.setLabel("Submit");
  submitButton.setWindow(dialogWindow);

  filenameField.setFocus(true);

}

public void execFilename(String filename)
{
  println("Filename event: "+ filename);
  if (filename != null && filename.length() <= 12)
  {
    setStoreFilename(filename);
    sendMachineExecMode();
  }
}
public void submitExecFilenameWindow(int theValue) 
{
  Textfield tf = (Textfield) cp5.controller("execFilename");
  tf.submit();
}

public void button_mode_sendMachineLiveMode()
{
  sendMachineLiveMode();
}





/*------------------------------------------------------------------------
    Details about the "drawing" subwindow
------------------------------------------------------------------------*/
public void button_mode_drawPixelsDialog()
{
  this.dialogWindow = cp5.addControlWindow("drawPixelsWindow",100,100,450,150);
  dialogWindow.hideCoordinates();
  
  dialogWindow.setBackground(getBackgroundColour());

  Radio rPos = cp5.addRadio("radio_startPosition",10,10);
  rPos.add("Top-right", DRAW_DIR_NE);
  rPos.add("Bottom-right", DRAW_DIR_SE);
  rPos.add("Bottom-left", DRAW_DIR_SW);
  rPos.add("Top-left", DRAW_DIR_NW);
  rPos.setWindow(dialogWindow);

  Radio rSkip = cp5.addRadio("radio_pixelSkipStyle",10,100);
  rSkip.add("Lift pen over masked pixels", 1);
  rSkip.add("Draw masked pixels as blanks", 2);
  rSkip.setWindow(dialogWindow);

//  Radio rDir = cp5.addRadio("radio_rowStartDirection",100,10);
//  rDir.add("Upwards", 0);
//  rDir.add("Downwards", 1);
//  rDir.setWindow(dialogWindow);

  Radio rStyle = cp5.addRadio("radio_pixelStyle",100,10);
  rStyle.add("Variable frequency square wave", PIXEL_STYLE_SQ_FREQ);
  rStyle.add("Variable size square wave", PIXEL_STYLE_SQ_SIZE);
  rStyle.add("Solid square wave", PIXEL_STYLE_SQ_SOLID);
  rStyle.add("Scribble", PIXEL_STYLE_SCRIBBLE);
  if (currentHardware >= HARDWARE_VER_MEGA)
  {
    rStyle.add("Spiral", PIXEL_STYLE_CIRCLE);
    rStyle.add("Sawtooth", PIXEL_STYLE_SAW);
  }
  rStyle.setWindow(dialogWindow);

  Button submitButton = cp5.addButton("submitDrawWindow",0,280,10,120,20);
  submitButton.setLabel("Generate commands");
  submitButton.setWindow(dialogWindow);
  

}

public Integer renderStartPosition = DRAW_DIR_NE; // default top right hand corner for start
public Integer renderStartDirection = DRAW_DIR_SE; // default start drawing in SE direction (DOWN)
public Integer renderStyle = PIXEL_STYLE_SQ_FREQ; // default pixel style square wave
public void radio_startPosition(int pos)
{
  this.renderStartPosition = pos;
  radio_rowStartDirection(1);
}
public void radio_rowStartDirection(int dir)
{
  if (renderStartPosition == DRAW_DIR_NE || renderStartPosition == DRAW_DIR_SW)
    renderStartDirection = (dir == 0) ? DRAW_DIR_NW : DRAW_DIR_SE;
  else if (renderStartPosition == DRAW_DIR_SE || renderStartPosition == DRAW_DIR_NW)
    renderStartDirection = (dir == 0) ? DRAW_DIR_NE : DRAW_DIR_SW;
}
public void radio_pixelStyle(int style)
{
  renderStyle = style;
}
public void radio_pixelSkipStyle(int style)
{
  if (style == 1)
    liftPenOnMaskedPixels = true;
  else if (style == 2)
    liftPenOnMaskedPixels = false;
}
public void submitDrawWindow(int theValue) 
{
  println("draw.");
  println("Style: " + renderStyle);
  println("Start pos: " + renderStartPosition);
  println("Start dir: " + renderStartDirection);
 
  switch (renderStyle)
  {
    case PIXEL_STYLE_SQ_FREQ: button_mode_renderSquarePixel(); break;
    case PIXEL_STYLE_SQ_SIZE: button_mode_renderScaledSquarePixels(); break;
    case PIXEL_STYLE_SQ_SOLID: button_mode_renderSolidSquarePixels(); break;
    case PIXEL_STYLE_SCRIBBLE: button_mode_renderScribblePixels(); break;
    case PIXEL_STYLE_CIRCLE: button_mode_renderCirclePixel(); break;
    case PIXEL_STYLE_SAW: button_mode_renderSawPixel(); break;
  }
  
   
}

/*------------------------------------------------------------------------
    Details about the "writing" subwindow
------------------------------------------------------------------------*/
String textToWrite = "";
String spriteFilePrefix = "sprite/let";
String spriteFileSuffix = ".txt";

public void button_mode_drawWritingDialog()
{
  this.dialogWindow = cp5.addControlWindow("drawWritingWindow",100,100,450,200);
  dialogWindow.hideCoordinates();
  
  dialogWindow.setBackground(getBackgroundColour());

  Textfield spriteFileField = cp5.addTextfield("spriteFilePrefixField",20,20,150,20);
  spriteFileField.setText(getSpriteFilePrefix());
  spriteFileField.setLabel("File prefix");
  spriteFileField.setWindow(dialogWindow);

  Textfield writingField = cp5.addTextfield("textToWriteField",20,60,400,20);
  writingField.setText(getTextToWrite());
  writingField.setLabel("Text to write");
  writingField.setWindow(dialogWindow);

  Button importTextButton = cp5.addButton("importTextButton",0,20,100,120,20);
  importTextButton.setLabel("Load text from file");
  importTextButton.setWindow(dialogWindow);

  Radio rPos = cp5.addRadio("radio_drawWritingDirection",20,140);
//  rPos.add("North-east", DRAW_DIR_NE);
  rPos.add("South-east", DRAW_DIR_SE);
//  rPos.add("South-west", DRAW_DIR_SW);
//  rPos.add("North-west", DRAW_DIR_NW);
  rPos.setWindow(dialogWindow);
  


  Button submitButton = cp5.addButton("submitWritingWindow",0,300,100,120,20);
  submitButton.setLabel("Generate commands");
  submitButton.setWindow(dialogWindow);
}

public void spriteFilePrefixField(String value)
{
  spriteFilePrefix = value;
}
public void textToWriteField(String value)
{
  textToWrite = value;
}

public String getTextToWrite()
{
  return textToWrite;
}
public String getSpriteFilePrefix()
{
  return spriteFilePrefix;
}
public String getSpriteFileSuffix()
{
  return spriteFileSuffix;
}

public void importTextButton()
{
  textToWrite = importTextToWriteFromFile();
  Textfield tf = (Textfield) cp5.controller("textToWriteField");
  tf.setText(getTextToWrite());
  tf.submit();
}


public void submitWritingWindow(int theValue) 
{
  println("Write.");
  
  Textfield tf = (Textfield) cp5.controller("spriteFilePrefixField");
  tf.submit();
  tf.setText(getSpriteFilePrefix());
  tf = (Textfield) cp5.controller("textToWriteField");
  tf.submit();
  tf.setText(getTextToWrite());
  
  println("Start dir: " + renderStartDirection);
  println("Sprite file prefix: " + spriteFilePrefix);
  println("Text: " + textToWrite);

  for (int i=0; i<getTextToWrite().length(); i++)
  {
    String filename = getSpriteFilePrefix() + (int) getTextToWrite().charAt(i) + getSpriteFileSuffix();
    addToCommandQueue(CMD_DRAW_SPRITE + PApplet.parseInt(gridSize * pixelScalingOverGridSize) + "," + filename+",END");
    println(filename);
  }
  
}


/*------------------------------------------------------------------------
    Details about the "sprite" subwindow
------------------------------------------------------------------------*/
String spriteFilename;
int minSpriteSize = 100;
int maxSpriteSize = 500;

public void button_mode_drawSpriteDialog()
{
  this.dialogWindow = cp5.addControlWindow("drawSpriteWindow",100,100,450,200);
  dialogWindow.hideCoordinates();
  
  dialogWindow.setBackground(getBackgroundColour());

  delay(200);
  Textfield spriteFilenameField = cp5.addTextfield("spriteFilenameField",20,20,400,20);
  spriteFilenameField.setText("filename.txt");
  spriteFilenameField.setLabel("Sprite filename");
  spriteFilenameField.setWindow(dialogWindow);

  Numberbox minSizeField = cp5.addNumberbox("minimumSpriteSize",20,60,100,20);
  minSizeField.setValue(getMinimumSpriteSize());
  minSizeField.setMin(10);
  minSizeField.setMax(getMaximumSpriteSize());
  minSizeField.setMultiplier(0.5f);  
  minSizeField.setLabel("Minimum size");
  minSizeField.setWindow(dialogWindow);

  Numberbox maxSizeField = cp5.addNumberbox("maximumSpriteSize",20,100,100,20);
  maxSizeField.setValue(getMaximumSpriteSize());
  maxSizeField.setMin(getMinimumSpriteSize());
  maxSizeField.setMultiplier(0.5f);  
  maxSizeField.setLabel("Maximum size");
  maxSizeField.setWindow(dialogWindow);

  Radio rPos = cp5.addRadio("radio_drawWritingDirection",20,140);
  rPos.add("North-east", DRAW_DIR_NE);
  rPos.add("South-east", DRAW_DIR_SE);
  rPos.add("South-west", DRAW_DIR_SW);
  rPos.add("North-west", DRAW_DIR_NW);
  rPos.setWindow(dialogWindow);
  


  Button submitButton = cp5.addButton("submitSpriteWindow",0,300,100,120,20);
  submitButton.setLabel("Draw sprite");
  submitButton.setWindow(dialogWindow);
}

public void radio_drawWritingDirection(int dir)
{
  renderStartDirection = dir;
}

public String getSpriteFilename()
{
  return spriteFilename;
}
public int getMinimumSpriteSize()
{
  return minSpriteSize;
}
public int getMaximumSpriteSize()
{
  return maxSpriteSize;
}

public void submitSpriteWindow(int theValue) 
{
  println("Sprite.");
  
  Textfield tf = (Textfield) cp5.controller("spriteFilenameField");
  tf.submit();
  tf.setText(getSpriteFilename());
  
  println("Start dir: " + renderStartDirection);
  println("Filename: " + spriteFilename);

  addToCommandQueue(CMD_DRAW_SPRITE + "," + spriteFilename + "," 
  + getMinimumSpriteSize() + "," + getMaximumSpriteSize() + "," + renderStartDirection + ",END");
  
}


/*------------------------------------------------------------------------
    Details about the "norwegian draw" subwindow
------------------------------------------------------------------------*/
String norwegianExecFilename = "filename.pbm";
int norwegianAmplitude = 20;
int norwegianWavelength = 2;

public void button_mode_drawNorwegianDialog()
{
  this.dialogWindow = cp5.addControlWindow("chooseNorwegianFilenameWindow",100,100,450,150);
  dialogWindow.hideCoordinates();
  
  dialogWindow.setBackground(getBackgroundColour());

  Textfield filenameField = cp5.addTextfield("norwegianExecFilename",20,20,150,20);
  filenameField.setText(norwegianExecFilename);
  filenameField.setLabel("Filename to execute from");
  filenameField.setWindow(dialogWindow);

  Numberbox minSizeField = cp5.addNumberbox("norwegianAmplitude",20,60,100,20);
  minSizeField.setValue(norwegianAmplitude);
  minSizeField.setMin(10);
  minSizeField.setMultiplier(0.5f);  
  minSizeField.setLabel("Amplitude");
  minSizeField.setWindow(dialogWindow);

  Numberbox maxSizeField = cp5.addNumberbox("norwegianWavelength",20,100,100,20);
  maxSizeField.setValue(norwegianWavelength);
  maxSizeField.setMin(1);
  maxSizeField.setMultiplier(0.5f);  
  maxSizeField.setLabel("Wavelength");
  maxSizeField.setWindow(dialogWindow);

  Button outlineButton = cp5.addButton("submitNorwegianExecTraceOutline",0,180,20,80,20);
  outlineButton.setLabel("Trace outline");
  outlineButton.setWindow(dialogWindow);

  Button submitButton = cp5.addButton("submitNorwegianExecFilenameWindow",0,180,100,80,20);
  submitButton.setLabel("Submit");
  submitButton.setWindow(dialogWindow);

  filenameField.setFocus(true);

}

public void submitNorwegianExecTraceOutline(int theValue) 
{
  Textfield tf = (Textfield) cp5.controller("norwegianExecFilename");
  tf.submit();
  tf.setText(norwegianExecFilename);
  
  println("Filename:" + norwegianExecFilename);
  
  addToCommandQueue(CMD_DRAW_NORWEGIAN_OUTLINE + norwegianExecFilename + ",END");
}

public void submitNorwegianExecFilenameWindow(int theValue) 
{
  Textfield tf = (Textfield) cp5.controller("norwegianExecFilename");
  tf.submit();
  tf.setText(norwegianExecFilename);
  
  println("Filename:" + norwegianExecFilename);
  println("Amplitude:" + norwegianAmplitude);
  println("Wavelength:" + norwegianWavelength);
  
  addToCommandQueue(CMD_DRAW_NORWEGIAN + norwegianExecFilename + ","+norwegianAmplitude+","+norwegianWavelength+",END");
}



/**
 Polargraph controller
 Copyright Sandy Noble 2012.
 
 This file is part of Polargraph Controller.
 
 Polargraph Controller is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Polargraph Controller is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
 
 Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
 Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
 
 This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.
 
 sandy.noble@gmail.com
 http://www.polargraph.co.uk/
 http://code.google.com/p/polargraph/
 */
public Set<String> getPanelNames()
{
  if (this.panelNames == null)
    this.panelNames = buildPanelNames();
  return this.panelNames;
}
public List<String> getTabNames()
{
  if (this.tabNames == null)
    this.tabNames = buildTabNames();
  return this.tabNames;
}
public Set<String> getControlNames()
{
  if (this.controlNames == null)
    this.controlNames = buildControlNames();
  return this.controlNames;
}
public Map<String, List<Controller>> getControlsForPanels()
{
  if (this.controlsForPanels == null)
    this.controlsForPanels = buildControlsForPanels();
  return this.controlsForPanels;
}
public Map<String, Controller> getAllControls()
{
  if (this.allControls == null)
    this.allControls = buildAllControls();
  return this.allControls;
}
public Map<String, String> getControlLabels()
{
  if (this.controlLabels == null)
    this.controlLabels = buildControlLabels();
  return this.controlLabels;
}
public Map<String, Set<Panel>> getPanelsForTabs()
{
  if (this.panelsForTabs == null)
    this.panelsForTabs = buildPanelsForTabs();
  return this.panelsForTabs;
}
public Map<String, Panel> getPanels()
{
  if (this.panels == null)
    this.panels = buildPanels();
  return this.panels;
}

public Set<String> getControlsToLockIfBoxNotSpecified()
{
  if (this.controlsToLockIfBoxNotSpecified == null)
  {
    this.controlsToLockIfBoxNotSpecified = buildControlsToLockIfBoxNotSpecified();
  }
  return this.controlsToLockIfBoxNotSpecified;
}

public Set<String> getControlsToLockIfImageNotLoaded()
{
  if (this.controlsToLockIfImageNotLoaded == null)
  {
    this.controlsToLockIfImageNotLoaded = buildControlsToLockIfImageNotLoaded();
  }
  return this.controlsToLockIfImageNotLoaded;
}


public void hideAllControls()
{
  for (String key : allControls.keySet())
  {
    Controller c = allControls.get(key);
    c.hide();
  }
}

public Map<String, Panel> buildPanels()
{
  Map<String, Panel> panels = new HashMap<String, Panel>();

  float panelHeight = frame.getHeight() - getMainPanelPosition().y - (DEFAULT_CONTROL_SIZE.y*3);
  Rectangle panelOutline = new Rectangle(getMainPanelPosition(), 
  new PVector((DEFAULT_CONTROL_SIZE.x+CONTROL_SPACING.x)*2, panelHeight));
  Panel inputPanel = new Panel(PANEL_NAME_INPUT, panelOutline);
  inputPanel.setResizable(true);
  inputPanel.setOutlineColour(color(200, 200, 200));
  // get controls
  inputPanel.setControls(getControlsForPanels().get(PANEL_NAME_INPUT));
  // get control positions
  inputPanel.setControlPositions(buildControlPositionsForPanel(inputPanel));
  inputPanel.setControlSizes(buildControlSizesForPanel(inputPanel));
  panels.put(PANEL_NAME_INPUT, inputPanel);

  Panel rovingPanel = new Panel(PANEL_NAME_ROVING, panelOutline);
  rovingPanel.setOutlineColour(color(200,200,200));
  // get controls
  rovingPanel.setResizable(true);
  rovingPanel.setControls(getControlsForPanels().get(PANEL_NAME_ROVING));
  // get control positions
  rovingPanel.setControlPositions(buildControlPositionsForPanel(rovingPanel));
  rovingPanel.setControlSizes(buildControlSizesForPanel(rovingPanel));
  panels.put(PANEL_NAME_ROVING, rovingPanel);

  Panel tracePanel = new Panel(PANEL_NAME_TRACE, panelOutline);
  tracePanel.setOutlineColour(color(200,200,200));
  // get controls
  tracePanel.setResizable(true);
  tracePanel.setControls(getControlsForPanels().get(PANEL_NAME_TRACE));
  // get control positions
  tracePanel.setControlPositions(buildControlPositionsForPanel(tracePanel));
  tracePanel.setControlSizes(buildControlSizesForPanel(tracePanel));
  panels.put(PANEL_NAME_TRACE, tracePanel);

  Panel detailsPanel = new Panel(PANEL_NAME_DETAILS, panelOutline);
  detailsPanel.setOutlineColour(color(200, 200, 200));
  // get controls
  detailsPanel.setResizable(true);
  detailsPanel.setControls(getControlsForPanels().get(PANEL_NAME_DETAILS));
  // get control positions
  detailsPanel.setControlPositions(buildControlPositionsForPanel(detailsPanel));
  detailsPanel.setControlSizes(buildControlSizesForPanel(detailsPanel));
  panels.put(PANEL_NAME_DETAILS, detailsPanel);

  Panel queuePanel = new Panel(PANEL_NAME_QUEUE, panelOutline);
  queuePanel.setOutlineColour(color(200, 200, 200));
  // get controls
  queuePanel.setResizable(true);
  queuePanel.setControls(getControlsForPanels().get(PANEL_NAME_QUEUE));
  // get control positions
  queuePanel.setControlPositions(buildControlPositionsForPanel(queuePanel));
  queuePanel.setControlSizes(buildControlSizesForPanel(queuePanel));
  panels.put(PANEL_NAME_QUEUE, queuePanel);

  panelOutline = new Rectangle(
    new PVector(getMainPanelPosition().x, getMainPanelPosition().y-((DEFAULT_CONTROL_SIZE.y+CONTROL_SPACING.y)*2)), 
    new PVector((DEFAULT_CONTROL_SIZE.x+CONTROL_SPACING.x)*2, (DEFAULT_CONTROL_SIZE.y+CONTROL_SPACING.y)*2));
  Panel generalPanel = new Panel(PANEL_NAME_GENERAL, panelOutline);
  generalPanel.setResizable(false);
  generalPanel.setOutlineColour(color(200, 200, 200));
  // get controls
  generalPanel.setControls(getControlsForPanels().get(PANEL_NAME_GENERAL));
  // get control positions
  generalPanel.setControlPositions(buildControlPositionsForPanel(generalPanel));
  generalPanel.setControlSizes(buildControlSizesForPanel(generalPanel));
  panels.put(PANEL_NAME_GENERAL, generalPanel);

  return panels;
}

public PVector getMainPanelPosition()
{
  return this.mainPanelPosition;
}

public void updateNumberboxValues()
{
  initialiseNumberboxValues(getAllControls());
}

public Set<String> buildControlsToLockIfBoxNotSpecified()
{
  Set<String> result = new HashSet<String>();
  result.add(MODE_DRAW_OUTLINE_BOX);
  result.add(MODE_DRAW_OUTLINE_BOX_ROWS);
  result.add(MODE_DRAW_SHADE_BOX_ROWS_PIXELS);
  result.add(MODE_RENDER_SQUARE_PIXELS);
  result.add(MODE_RENDER_SCALED_SQUARE_PIXELS);
  result.add(MODE_RENDER_SAW_PIXELS);
  result.add(MODE_RENDER_CIRCLE_PIXELS);
  result.add(MODE_RENDER_PIXEL_DIALOG);
  result.add(MODE_DRAW_GRID);
  result.add(MODE_DRAW_TESTPATTERN);
  result.add(MODE_RENDER_SOLID_SQUARE_PIXELS);
  result.add(MODE_RENDER_SCRIBBLE_PIXELS);
  result.add(MODE_CONVERT_BOX_TO_PICTUREFRAME);
  result.add(MODE_IMAGE_PIXEL_BRIGHT_THRESHOLD);
  result.add(MODE_IMAGE_PIXEL_DARK_THRESHOLD);

  return result;
}

public Set<String> buildControlsToLockIfImageNotLoaded()
{
  Set<String> result = new HashSet<String>();
  result.add(MODE_MOVE_IMAGE);
  result.add(MODE_RESIZE_IMAGE);
//  result.add(MODE_INPUT_BOX_TOP_LEFT);
  result.add(MODE_CHANGE_GRID_SIZE);
  result.add(MODE_CHANGE_SAMPLE_AREA);
  result.add(MODE_SELECT_PICTUREFRAME);

  return result;
}

public Map<String, Controller> buildAllControls()
{

  Map<String, Controller> map = new HashMap<String, Controller>();

  for (String controlName : getControlNames())
  {
    if (controlName.startsWith("button_"))
    {
      Button b = cp5.addButton(controlName, 0, 100, 100, 100, 100);
      b.setLabel(getControlLabels().get(controlName));
      b.hide();
      map.put(controlName, b);
      //      println("Added button " + controlName);
    }
    else if (controlName.startsWith("toggle_"))
    {
      Toggle t = cp5.addToggle(controlName, false, 100, 100, 100, 100);
      t.setLabel(getControlLabels().get(controlName));
      t.hide();
      controlP5.Label l = t.captionLabel();
      l.style().marginTop = -17; //move upwards (relative to button size)
      l.style().marginLeft = 4; //move to the right
      map.put(controlName, t);
      //      println("Added toggle " + controlName);
    }
    else if (controlName.startsWith("minitoggle_"))
    {
      Toggle t = cp5.addToggle(controlName, false, 100, 100, 100, 100);
      t.setLabel(getControlLabels().get(controlName));
      t.hide();
      controlP5.Label l = t.captionLabel();
      l.style().marginTop = -17; //move upwards (relative to button size)
      l.style().marginLeft = 4; //move to the right
      map.put(controlName, t);
      //      println("Added minitoggle " + controlName);
    }
    else if (controlName.startsWith("numberbox_"))
    {
      Numberbox n = cp5.addNumberbox(controlName, 100, 100, 100, 100, 20);
      n.setLabel(getControlLabels().get(controlName));
      n.hide();
      n.setDecimalPrecision(0);
      controlP5.Label l = n.captionLabel();
      l.style().marginTop = -17; //move upwards (relative to button size)
      l.style().marginLeft = 40; //move to the right
      // change the control direction to left/right
      n.setDirection(Controller.VERTICAL);
      map.put(controlName, n);
      //      println("Added numberbox " + controlName);
    }
  }

  initialiseToggleValues(map);
  initialiseNumberboxValues(map);
  return map;
}

public Map<String, Controller> initialiseNumberboxValues(Map<String, Controller> map)
{
  for (String key : map.keySet())
  {
    if (key.startsWith("numberbox_"))
    {
      Numberbox n = (Numberbox) map.get(key);

      if (MODE_CHANGE_SAMPLE_AREA.equals(key))
      {
        n.setValue(getSampleArea());
        n.setMin(1);
        n.setMultiplier(1);
      }
      else if (MODE_CHANGE_GRID_SIZE.equals(key))
      {
        n.setValue(getGridSize());
        n.setMin(20);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_MACHINE_WIDTH.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getDisplayMachine().getWidth()));
        n.setMin(20);
        n.setMultiplier(0.5f);
      }
      else if (MODE_RESIZE_IMAGE.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getDisplayMachine().getImageFrame().getWidth()));
        n.setMin(20);
        n.setMultiplier(1);
      }
      else if (MODE_CHANGE_MACHINE_HEIGHT.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getDisplayMachine().getHeight()));
        n.setMin(20);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_MM_PER_REV.equals(key))
      {
        n.setValue(getDisplayMachine().getMMPerRev());
        n.setMin(20);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_STEPS_PER_REV.equals(key))
      {
        n.setValue(getDisplayMachine().getStepsPerRev());
        n.setMin(20);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_STEP_MULTIPLIER.equals(key))
      {
        n.setValue(machineStepMultiplier);
        n.setMin(1);
        n.setMax(16);
        n.setMultiplier(0.01f);
      }
      else if (MODE_CHANGE_PAGE_WIDTH.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getDisplayMachine().getPage().getWidth()));
        n.setMin(10);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_PAGE_HEIGHT.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getDisplayMachine().getPage().getHeight()));
        n.setMin(10);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_PAGE_OFFSET_X.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getDisplayMachine().getPage().getLeft()));
        n.setMin(0);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_PAGE_OFFSET_Y.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getDisplayMachine().getPage().getTop()));
        n.setMin(0);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_HOMEPOINT_X.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getHomePoint().x));
        n.setMin(0);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_HOMEPOINT_Y.equals(key))
      {
        n.setValue(getDisplayMachine().inMM(getHomePoint().y));
        n.setMin(0);
        n.setMultiplier(0.5f);
      }
      else if (MODE_CHANGE_PEN_WIDTH.equals(key))
      {
        n.setDecimalPrecision(2);
        n.setValue(currentPenWidth);
        n.setMin(0.01f);
        n.setMultiplier(0.01f);
      }
      else if (MODE_CHANGE_PEN_TEST_START_WIDTH.equals(key))
      {
        n.setDecimalPrecision(2);
        n.setValue(testPenWidthStartSize);
        n.setMin(0.01f);
        n.setMultiplier(0.01f);
      }
      else if (MODE_CHANGE_PEN_TEST_END_WIDTH.equals(key))
      {
        n.setDecimalPrecision(2);
        n.setValue(testPenWidthEndSize);
        n.setMin(0.01f);
        n.setMultiplier(0.01f);
      }
      else if (MODE_CHANGE_PEN_TEST_INCREMENT_SIZE.equals(key))
      {
        n.setDecimalPrecision(2);
        n.setValue(testPenWidthIncrementSize);
        n.setMin(0.01f);
        n.setMultiplier(0.01f);
      }
      else if (MODE_CHANGE_MACHINE_MAX_SPEED.equals(key))
      {
        n.setDecimalPrecision(0);
        n.setValue(currentMachineMaxSpeed);
        n.setMin(1);
        n.setMultiplier(1);
      }
      else if (MODE_CHANGE_MACHINE_ACCELERATION.equals(key))
      {
        n.setDecimalPrecision(0);
        n.setValue(currentMachineAccel);
        n.setMin(1);
        n.setMultiplier(1);
      }
      else if (MODE_IMAGE_PIXEL_BRIGHT_THRESHOLD.equals(key))
      {
        n.setDecimalPrecision(0);
        n.setValue(pixelExtractBrightThreshold);
        n.setMin(0);
        n.setMax(255);
        n.setMultiplier(0.5f);
      }
      else if (MODE_IMAGE_PIXEL_DARK_THRESHOLD.equals(key))
      {
        n.setDecimalPrecision(0);
        n.setValue(pixelExtractDarkThreshold);
        n.setMin(0);
        n.setMax(255);
        n.setMultiplier(0.5f);
      }
      else if (MODE_RESIZE_VECTOR.equals(key))
      {
        n.setDecimalPrecision(1);
        n.setValue(vectorScaling);
        n.setMin(0.1f);
        n.setMax(1000);
        n.setMultiplier(0.5f);
      }      
      else if (MODE_CHANGE_PIXEL_SCALING.equals(key))
      {
        n.setDecimalPrecision(2);
        n.setValue(pixelScalingOverGridSize);
        n.setMin(0.1f);
        n.setMax(4.0f);
        n.setMultiplier(0.01f);
      }
      else if (MODE_CHANGE_MIN_VECTOR_LINE_LENGTH.equals(key))
      {
        n.setValue(minimumVectorLineLength);
        n.setMin(0);
        n.setMultiplier(1);
      }
      else if (MODE_PEN_LIFT_POS_UP.equals(key))
      {
        n.setDecimalPrecision(1);
        n.setValue(penLiftUpPosition);
        n.setMin(0);
        n.setMax(360);
        n.setMultiplier(1);
      }
      else if (MODE_PEN_LIFT_POS_DOWN.equals(key))
      {
        n.setDecimalPrecision(1);
        n.setValue(penLiftDownPosition);
        n.setMin(0);
        n.setMax(360);
        n.setMultiplier(0.1f);
      }
      else if (MODE_LIVE_BLUR_VALUE.equals(key))
      {
        n.setDecimalPrecision(1);
        n.setValue(blurValue);
        n.setMin(1);
        n.setMax(10);
        n.setMultiplier(0.1f);
      }
      else if (MODE_LIVE_SIMPLIFICATION_VALUE.equals(key))
      {
        n.setDecimalPrecision(1);
        n.setValue(liveSimplification);
        n.setMin(LIVE_SIMPLIFICATION_MIN);
        n.setMax(LIVE_SIMPLIFICATION_MAX);
        n.setMultiplier(0.1f);
      }
      else if (MODE_LIVE_POSTERISE_VALUE.equals(key))
      {
        n.setDecimalPrecision(1);
        n.setValue(posterizeValue);
        n.setMin(2);
        n.setMax(32);
        n.setMultiplier(0.1f);
      }
      else if (MODE_VECTOR_PATH_LENGTH_HIGHPASS_CUTOFF.equals(key))
      {
        n.setDecimalPrecision(1);
        n.setValue(pathLengthHighPassCutoff);
        n.setMin(PATH_LENGTH_HIGHPASS_CUTOFF_MIN);
        n.setMax(PATH_LENGTH_HIGHPASS_CUTOFF_MAX);
        n.setMultiplier(0.5f);
      }
    }
  }
  return map;
}


public Map<String, Controller> initialiseToggleValues(Map<String, Controller> map)
{
  for (String key : map.keySet())
  {
    if (MODE_SHOW_DENSITY_PREVIEW.equals(key))
    {
      Toggle t = (Toggle) map.get(key);
      t.setValue((displayingDensityPreview) ? 1 : 0);
    }
    else if (MODE_SHOW_QUEUE_PREVIEW.equals(key))
    {
      Toggle t = (Toggle) map.get(key);
      t.setValue((displayingQueuePreview) ? 1 : 0);
    }
    else if (MODE_SHOW_IMAGE.equals(key))
    {
      Toggle t = (Toggle) map.get(key);
      t.setValue((displayingImage) ? 1 : 0);
    }
    else if (MODE_SHOW_VECTOR.equals(key))
    {
      Toggle t = (Toggle) map.get(key);
      t.setValue((displayingVector) ? 1 : 0);
    }
    else if (MODE_SHOW_GUIDES.equals(key))
    {
      Toggle t = (Toggle) map.get(key);
      t.setValue((displayingGuides) ? 1 : 0);
    }
    else if (MODE_SHOW_WEBCAM_RAW_VIDEO.equals(key))
    {
      Toggle t = (Toggle) map.get(key);
//      t.setValue((drawingLiveVideo) ? 1 : 0);
    }
    else if (MODE_FLIP_WEBCAM_INPUT.equals(key))
    {
      Toggle t = (Toggle) map.get(key);
      t.setValue((flipWebcamImage) ? 1 : 0);
    }
    else if (MODE_ROTATE_WEBCAM_INPUT.equals(key))
    {
      Toggle t = (Toggle) map.get(key);
      t.setValue((rotateWebcamImage) ? 1 : 0);
    }
    
  }
  return map;
}




public String getControlLabel(String butName)
{
  if (controlLabels.containsKey(butName))
    return controlLabels.get(butName);
  else
    return "";
}

public Map<String, PVector> buildControlPositionsForPanel(Panel panel)
{
  Map<String, PVector> map = new HashMap<String, PVector>();
  String panelName = panel.getName();
  int col = 0;
  int row = 0;
  for (Controller controller : panel.getControls())
  {
    if (controller.name().startsWith("minitoggle_"))
    {
      PVector p = new PVector(col*(DEFAULT_CONTROL_SIZE.x+CONTROL_SPACING.x), row*(DEFAULT_CONTROL_SIZE.y+CONTROL_SPACING.y));
      map.put(controller.name(), p);
      row++;
      if (p.y + (DEFAULT_CONTROL_SIZE.y*2) >= panel.getOutline().getHeight())
      {
        row = 0;
        col++;
      }
    }
    else
    {
      PVector p = new PVector(col*(DEFAULT_CONTROL_SIZE.x+CONTROL_SPACING.x), row*(DEFAULT_CONTROL_SIZE.y+CONTROL_SPACING.y));
      map.put(controller.name(), p);
      row++;
      if (p.y + (DEFAULT_CONTROL_SIZE.y*2) >= panel.getOutline().getHeight())
      {
        row = 0;
        col++;
      }
    }
  }

  return map;
}
public Map<String, PVector> buildControlSizesForPanel(Panel panel)
{
  //println("Building control sizes for panel " + panel.getName());
  Map<String, PVector> map = new HashMap<String, PVector>();
  String panelName = panel.getName();
  int col = 0;
  int row = 0;
  for (Controller controller : panel.getControls())
  {
    if (controller.name().startsWith("minitoggle_"))
    {
      PVector s = new PVector(DEFAULT_CONTROL_SIZE.y, DEFAULT_CONTROL_SIZE.y);
      map.put(controller.name(), s);
    }
    else
    {
      PVector s = new PVector(DEFAULT_CONTROL_SIZE.x, DEFAULT_CONTROL_SIZE.y);
      map.put(controller.name(), s);
      //println("Added size of " + controller.name() + " to panel. " + s);
    }
  }

  return map;
}


public Map<String, List<Controller>> buildControlsForPanels()
{
//  println("build controls for panels.");
  Map<String, List<Controller>> map = new HashMap<String, List<Controller>>();
  map.put(PANEL_NAME_INPUT, getControllersForControllerNames(getControlNamesForInputPanel()));
  map.put(PANEL_NAME_ROVING, getControllersForControllerNames(getControlNamesForRovingPanel()));
  map.put(PANEL_NAME_DETAILS, getControllersForControllerNames(getControlNamesForDetailPanel()));
  map.put(PANEL_NAME_QUEUE, getControllersForControllerNames(getControlNamesForQueuePanel()));
  map.put(PANEL_NAME_GENERAL, getControllersForControllerNames(getControlNamesForGeneralPanel()));
  map.put(PANEL_NAME_TRACE, getControllersForControllerNames(getControlNamesForTracePanel()));
  return map;
}

public List<Controller> getControllersForControllerNames(List<String> names)
{
  List<Controller> list = new ArrayList<Controller>();
  for (String name : names)
  {
    Controller c = getAllControls().get(name);
    if (c != null)
      list.add(c);
  }
  return list;
}

/* This creates a list of control names for the input panel. */
public List<String> getControlNamesForInputPanel()
{
  List<String> controlNames = new ArrayList<String>();
  controlNames.add(MODE_CLEAR_QUEUE);
  controlNames.add(MODE_SET_POSITION_HOME);
  controlNames.add(MODE_SET_POSITION);
  controlNames.add(MODE_DRAW_TO_POSITION);
  controlNames.add(MODE_DRAW_DIRECT);
  controlNames.add(MODE_RETURN_TO_HOME);
  controlNames.add(MODE_PEN_LIFT_UP);
  controlNames.add(MODE_PEN_LIFT_DOWN);
  controlNames.add(MODE_INPUT_BOX_TOP_LEFT);
  controlNames.add(MODE_CONVERT_BOX_TO_PICTUREFRAME);
  controlNames.add(MODE_SELECT_PICTUREFRAME);
  controlNames.add(MODE_LOAD_IMAGE);
  controlNames.add(MODE_MOVE_IMAGE);
  controlNames.add(MODE_RESIZE_IMAGE);
  controlNames.add(MODE_IMAGE_PIXEL_BRIGHT_THRESHOLD);
  controlNames.add(MODE_IMAGE_PIXEL_DARK_THRESHOLD);
  controlNames.add(MODE_CHANGE_GRID_SIZE);
  controlNames.add(MODE_CHANGE_SAMPLE_AREA);
  controlNames.add(MODE_CHOOSE_CHROMA_KEY_COLOUR);
  controlNames.add(MODE_CHANGE_PIXEL_SCALING);
  
  controlNames.add(MODE_RENDER_PIXEL_DIALOG);
  controlNames.add(MODE_DRAW_GRID);
  controlNames.add(MODE_DRAW_OUTLINE_BOX);
  controlNames.add(MODE_DRAW_OUTLINE_BOX_ROWS);
  controlNames.add(MODE_DRAW_SHADE_BOX_ROWS_PIXELS);

  controlNames.add(MODE_LOAD_VECTOR_FILE);
  controlNames.add(MODE_RESIZE_VECTOR);
  controlNames.add(MODE_MOVE_VECTOR);
  controlNames.add(MODE_CHANGE_MIN_VECTOR_LINE_LENGTH);
  //controlNames.add(MODE_VECTOR_PATH_LENGTH_HIGHPASS_CUTOFF);
  controlNames.add(MODE_RENDER_VECTORS);

  controlNames.add(MODE_SHOW_IMAGE);
  controlNames.add(MODE_SHOW_VECTOR);
  controlNames.add(MODE_SHOW_QUEUE_PREVIEW);
  controlNames.add(MODE_SHOW_DENSITY_PREVIEW);
  controlNames.add(MODE_SHOW_GUIDES);

  
  return controlNames;
}

public List<String> getControlNamesForRovingPanel()
{
  List<String> controlNames = new ArrayList<String>();
  controlNames.add(MODE_CLEAR_QUEUE);
  controlNames.add(MODE_INPUT_BOX_TOP_LEFT);
  controlNames.add(MODE_CONVERT_BOX_TO_PICTUREFRAME);
  controlNames.add(MODE_SELECT_PICTUREFRAME);
  controlNames.add(MODE_SEND_ROVE_AREA);
  controlNames.add(MODE_SEND_START_TEXT);
  controlNames.add(MODE_CHANGE_GRID_SIZE);
  controlNames.add(MODE_SHOW_WRITING_DIALOG);
  controlNames.add(MODE_START_SWIRLING);
  controlNames.add(MODE_STOP_SWIRLING);
  controlNames.add(MODE_START_MARKING);
  controlNames.add(MODE_STOP_MARKING);
  controlNames.add(MODE_SELECT_ROVE_IMAGE_SOURCE);
  controlNames.add(MODE_START_SPRITE);
  controlNames.add(MODE_START_RANDOM_SPRITES);
  controlNames.add(MODE_STOP_RANDOM_SPRITES);
  controlNames.add(MODE_DRAW_NORWEGIAN_DIALOG);
  
  
  return controlNames;
}

public List<String> getControlNamesForTracePanel()
{
  List<String> controlNames = new ArrayList<String>();
  controlNames.add(MODE_LIVE_BLUR_VALUE);
  controlNames.add(MODE_LIVE_SIMPLIFICATION_VALUE);
  controlNames.add(MODE_LIVE_POSTERISE_VALUE);
  controlNames.add(MODE_LIVE_CAPTURE_FROM_LIVE);
  controlNames.add(MODE_LIVE_CANCEL_CAPTURE);
//  controlNames.add(MODE_LIVE_ADD_CAPTION);
  controlNames.add(MODE_LIVE_CONFIRM_DRAW);
//  controlNames.add(MODE_VECTOR_PATH_LENGTH_HIGHPASS_CUTOFF);

//  controlNames.add(MODE_SHOW_WEBCAM_RAW_VIDEO);
//  controlNames.add(MODE_FLIP_WEBCAM_INPUT);
//  controlNames.add(MODE_ROTATE_WEBCAM_INPUT);
  return controlNames;
}

public List<String> getControlNamesForDetailPanel()
{
  List<String> controlNames = new ArrayList<String>();
  controlNames.add(MODE_CHANGE_MACHINE_SPEC);
  controlNames.add(MODE_REQUEST_MACHINE_SIZE);
  controlNames.add(MODE_RESET_MACHINE);

  controlNames.add(MODE_CHANGE_MM_PER_REV);
  controlNames.add(MODE_CHANGE_STEPS_PER_REV);
  controlNames.add(MODE_CHANGE_STEP_MULTIPLIER);
  controlNames.add(MODE_CHANGE_MACHINE_WIDTH);
  controlNames.add(MODE_CHANGE_MACHINE_HEIGHT);
  controlNames.add(MODE_CHANGE_PAGE_WIDTH);
  controlNames.add(MODE_CHANGE_PAGE_HEIGHT);
  controlNames.add(MODE_CHANGE_PAGE_OFFSET_X);
  controlNames.add(MODE_CHANGE_PAGE_OFFSET_Y);
  controlNames.add(MODE_CHANGE_PAGE_OFFSET_X_CENTRE);

  controlNames.add(MODE_CHANGE_HOMEPOINT_X);
  controlNames.add(MODE_CHANGE_HOMEPOINT_Y);
  controlNames.add(MODE_CHANGE_HOMEPOINT_X_CENTRE);

  controlNames.add(MODE_CHANGE_PEN_WIDTH);
  controlNames.add(MODE_SEND_PEN_WIDTH);

  controlNames.add(MODE_CHANGE_PEN_TEST_START_WIDTH);
  controlNames.add(MODE_CHANGE_PEN_TEST_END_WIDTH);
  controlNames.add(MODE_CHANGE_PEN_TEST_INCREMENT_SIZE);
  controlNames.add(MODE_DRAW_TEST_PENWIDTH);

  controlNames.add(MODE_PEN_LIFT_POS_UP);
  controlNames.add(MODE_PEN_LIFT_POS_DOWN);
  controlNames.add(MODE_SEND_PEN_LIFT_RANGE);
  controlNames.add(MODE_SEND_PEN_LIFT_RANGE_PERSIST);

  controlNames.add(MODE_CHANGE_MACHINE_MAX_SPEED);
  controlNames.add(MODE_CHANGE_MACHINE_ACCELERATION);
  controlNames.add(MODE_SEND_MACHINE_SPEED);
  controlNames.add(MODE_SEND_MACHINE_SPEED_PERSIST);

  controlNames.add(MODE_CHANGE_SERIAL_PORT);

  return controlNames;
}

public List<String> getControlNamesForQueuePanel()
{
  List<String> controlNames = new ArrayList<String>();
  controlNames.add(MODE_CLEAR_QUEUE);
  controlNames.add(MODE_EXPORT_QUEUE);
  controlNames.add(MODE_IMPORT_QUEUE);

  if (getHardwareVersion() >= HARDWARE_VER_MEGA)
  {
    controlNames.add(MODE_SEND_MACHINE_STORE_MODE);
    controlNames.add(MODE_SEND_MACHINE_LIVE_MODE);
    controlNames.add(MODE_SEND_MACHINE_EXEC_MODE);
  }

  return controlNames;
}

public List<String> getControlNamesForGeneralPanel()
{
  List<String> controlNames = new ArrayList<String>();
  controlNames.add(MODE_SAVE_PROPERTIES);
  controlNames.add(MODE_SAVE_AS_PROPERTIES);
  controlNames.add(MODE_LOAD_PROPERTIES);
  return controlNames;
}



public Map<String, String> buildControlLabels()
{
  Map<String, String> result = new HashMap<String, String>();

  result.put(MODE_BEGIN, "Reset queue");
  result.put(MODE_INPUT_BOX_TOP_LEFT, "Select Area");
  result.put(MODE_INPUT_BOX_BOT_RIGHT, "Select BotRight");
  result.put(MODE_DRAW_OUTLINE_BOX, "Draw Outline box");
  result.put(MODE_DRAW_OUTLINE_BOX_ROWS, "Draw Outline rows");
  result.put(MODE_DRAW_SHADE_BOX_ROWS_PIXELS, "Draw Outline pixels");
  result.put(MODE_DRAW_TO_POSITION, "Move pen to point");
  result.put(MODE_DRAW_DIRECT, "Move direct");
  result.put(MODE_RENDER_SQUARE_PIXELS, "Shade Squarewave");
  result.put(MODE_RENDER_SCALED_SQUARE_PIXELS, "Shade Scaled Square");
  result.put(MODE_RENDER_SAW_PIXELS, "Shade sawtooth");
  result.put(MODE_RENDER_CIRCLE_PIXELS, "Shade spiral");
  result.put(MODE_INPUT_ROW_START, "Select Row start");
  result.put(MODE_INPUT_ROW_END, "Select Row end");
  result.put(MODE_SET_POSITION, "Set pen position");
  result.put(MODE_DRAW_GRID, "Draw grid of box");
  result.put(MODE_DRAW_TESTPATTERN, "test pattern");
  result.put(MODE_PLACE_IMAGE, "place image");
  result.put(MODE_LOAD_IMAGE, "Load image file");
  result.put(MODE_SET_POSITION_HOME, "Set home");
  result.put(MODE_RETURN_TO_HOME, "Return to home");
  result.put(MODE_INPUT_SINGLE_PIXEL, "Choose pixel");
  result.put(MODE_DRAW_TEST_PENWIDTH, "Test pen widths");
  result.put(MODE_RENDER_SOLID_SQUARE_PIXELS, "Shade solid");
  result.put(MODE_RENDER_SCRIBBLE_PIXELS, "Shade scribble");

  result.put(MODE_CHANGE_MACHINE_SPEC, "Upload machine spec");
  result.put(MODE_REQUEST_MACHINE_SIZE, "Download size spec");
  result.put(MODE_RESET_MACHINE, "Reset machine to factory");
  result.put(MODE_SAVE_PROPERTIES, "Save");
  result.put(MODE_SAVE_AS_PROPERTIES, "Save as");
  result.put(MODE_LOAD_PROPERTIES, "Load config");

  result.put(MODE_INC_SAMPLE_AREA, "Inc sample size");
  result.put(MODE_DEC_SAMPLE_AREA, "Dec sample size");

  result.put(MODE_MOVE_IMAGE, "Move image");
  result.put(MODE_CONVERT_BOX_TO_PICTUREFRAME, "Set frame to area");
  result.put(MODE_SELECT_PICTUREFRAME, "Select frame");

  result.put(MODE_CLEAR_QUEUE, "Clear queue");
  result.put(MODE_EXPORT_QUEUE, "Export queue");
  result.put(MODE_IMPORT_QUEUE, "Import queue");
  result.put(MODE_RESIZE_IMAGE, "Resize image");

  result.put(MODE_RENDER_COMMAND_QUEUE, "Preview queue");

  result.put(MODE_CHANGE_GRID_SIZE, "Grid size");
  result.put(MODE_CHANGE_SAMPLE_AREA, "Sample area");

  result.put(MODE_SHOW_IMAGE, "Show image");
  result.put(MODE_SHOW_DENSITY_PREVIEW, "Show density preview");
  result.put(MODE_SHOW_QUEUE_PREVIEW, "Show Queue preview");
  result.put(MODE_SHOW_VECTOR, "Show Vector");
  result.put(MODE_SHOW_GUIDES, "Show Guides");

  result.put(MODE_CHANGE_MACHINE_WIDTH, "Machine Width");
  result.put(MODE_CHANGE_MACHINE_HEIGHT, "Machine Height");
  result.put(MODE_CHANGE_MM_PER_REV, "MM Per Rev");
  result.put(MODE_CHANGE_STEPS_PER_REV, "Steps Per Rev");
  result.put(MODE_CHANGE_STEP_MULTIPLIER, "Step multiplier");
  result.put(MODE_CHANGE_PAGE_WIDTH, "Page Width");
  result.put(MODE_CHANGE_PAGE_HEIGHT, "Page Height");
  result.put(MODE_CHANGE_PAGE_OFFSET_X, "Page Pos X");
  result.put(MODE_CHANGE_PAGE_OFFSET_Y, "Page Pos Y");
  result.put(MODE_CHANGE_PAGE_OFFSET_X_CENTRE, "Centre page");

  result.put(MODE_CHANGE_HOMEPOINT_X, "Home Pos X");
  result.put(MODE_CHANGE_HOMEPOINT_Y, "Home Pos Y");
  result.put(MODE_CHANGE_HOMEPOINT_X_CENTRE, "Centre Homepoint");

  result.put(MODE_CHANGE_PEN_WIDTH, "Pen tip size");
  result.put(MODE_SEND_PEN_WIDTH, "Send Pen tip size");

  result.put(MODE_CHANGE_PEN_TEST_START_WIDTH, "Pen test start tip");
  result.put(MODE_CHANGE_PEN_TEST_END_WIDTH, "Pen test end tip");
  result.put(MODE_CHANGE_PEN_TEST_INCREMENT_SIZE, "Pen test inc size");

  result.put(MODE_CHANGE_MACHINE_MAX_SPEED, "Motor max speed");
  result.put(MODE_CHANGE_MACHINE_ACCELERATION, "Motor acceleration");
  result.put(MODE_SEND_MACHINE_SPEED, "Send speed");
  result.put(MODE_SEND_MACHINE_SPEED_PERSIST, "Upload speed");
  result.put(MODE_RENDER_VECTORS, "Draw vectors");
  result.put(MODE_LOAD_VECTOR_FILE, "Load vector");
  result.put(MODE_CHANGE_MIN_VECTOR_LINE_LENGTH, "Shortest vector");

  result.put(MODE_IMAGE_PIXEL_BRIGHT_THRESHOLD, "Bright pixel");
  result.put(MODE_IMAGE_PIXEL_DARK_THRESHOLD, "Dark pixel");
  
  result.put(MODE_CHANGE_SERIAL_PORT, "Serial port...");

  result.put(MODE_SEND_MACHINE_STORE_MODE, "Signal store...");
  result.put(MODE_SEND_MACHINE_LIVE_MODE, "Signal play");
  result.put(MODE_SEND_MACHINE_EXEC_MODE, "Exec from store...");

  result.put(MODE_RESIZE_VECTOR, "Resize vector");
  result.put(MODE_MOVE_VECTOR, "Move vector");
  result.put(MODE_RENDER_PIXEL_DIALOG, "Render pixels...");
  result.put(MODE_CHOOSE_CHROMA_KEY_COLOUR, "Choose mask colour");
  result.put(MODE_CHANGE_PIXEL_SCALING, "Pixel scaling");
  
  result.put(MODE_PEN_LIFT_UP, "Pen lift");
  result.put(MODE_PEN_LIFT_DOWN, "Pen drop");
  result.put(MODE_PEN_LIFT_POS_UP, "Pen up position");
  result.put(MODE_PEN_LIFT_POS_DOWN, "Pen down position");
  result.put(MODE_SEND_PEN_LIFT_RANGE, "Test lift range");
  result.put(MODE_SEND_PEN_LIFT_RANGE_PERSIST, "Upload lift range");
  
  result.put(MODE_SEND_ROVE_AREA, "Send Roving Area");
  result.put(MODE_SELECT_ROVE_IMAGE_SOURCE, "Choose source image");
  result.put(MODE_SEND_START_TEXT, "Start text at point");
  result.put(MODE_SHOW_WRITING_DIALOG, "Render writing...");

  result.put(MODE_START_SWIRLING, "Swirl");
  result.put(MODE_STOP_SWIRLING, "Stop swirl");
  result.put(MODE_START_MARKING, "Mark");
  result.put(MODE_STOP_MARKING, "Stop marking");
  result.put(MODE_START_SPRITE, "Choose sprite...");
  result.put(MODE_START_RANDOM_SPRITES, "Random sprites");
  result.put(MODE_STOP_RANDOM_SPRITES, "Stop sprites");
  result.put(MODE_DRAW_NORWEGIAN_DIALOG, "Draw norwegian...");
  
  result.put(MODE_LIVE_BLUR_VALUE, "Blur");
  result.put(MODE_LIVE_SIMPLIFICATION_VALUE, "Simplify");
  result.put(MODE_LIVE_POSTERISE_VALUE, "Posterise");
  result.put(MODE_LIVE_CAPTURE_FROM_LIVE, "Capture");
  result.put(MODE_LIVE_CONFIRM_DRAW, "Draw capture");
  result.put(MODE_LIVE_CANCEL_CAPTURE, "Cancel capture");
  result.put(MODE_LIVE_ADD_CAPTION, "Add caption");
  
  result.put(MODE_VECTOR_PATH_LENGTH_HIGHPASS_CUTOFF, "Path length cutoff");
  result.put(MODE_SHOW_WEBCAM_RAW_VIDEO, "Show video");
  result.put(MODE_FLIP_WEBCAM_INPUT, "Flip video");
  result.put(MODE_ROTATE_WEBCAM_INPUT, "Rotate webcam");

  

  return result;
}

public Set<String> buildControlNames()
{
  Set<String> result = new HashSet<String>();
  result.add(MODE_BEGIN);
  result.add(MODE_INPUT_BOX_TOP_LEFT);
  result.add(MODE_INPUT_BOX_BOT_RIGHT);
  result.add(MODE_DRAW_OUTLINE_BOX);
  result.add(MODE_DRAW_OUTLINE_BOX_ROWS);
  result.add(MODE_DRAW_SHADE_BOX_ROWS_PIXELS);
  result.add(MODE_DRAW_TO_POSITION);
  result.add(MODE_DRAW_DIRECT);
  result.add(MODE_RENDER_SQUARE_PIXELS);
  result.add(MODE_RENDER_SCALED_SQUARE_PIXELS);
  result.add(MODE_RENDER_SAW_PIXELS);
  result.add(MODE_RENDER_CIRCLE_PIXELS);
  
  result.add(MODE_RENDER_PIXEL_DIALOG);
  
  result.add(MODE_INPUT_ROW_START);
  result.add(MODE_INPUT_ROW_END);
  result.add(MODE_SET_POSITION);
  result.add(MODE_DRAW_GRID);
  result.add(MODE_DRAW_TESTPATTERN);
  result.add(MODE_PLACE_IMAGE);
  result.add(MODE_LOAD_IMAGE);
  result.add(MODE_SET_POSITION_HOME);
  result.add(MODE_RETURN_TO_HOME);
  result.add(MODE_INPUT_SINGLE_PIXEL);
  result.add(MODE_DRAW_TEST_PENWIDTH);
  result.add(MODE_RENDER_SOLID_SQUARE_PIXELS);
  result.add(MODE_RENDER_SCRIBBLE_PIXELS);
  result.add(MODE_CHANGE_MACHINE_SPEC);
  result.add(MODE_REQUEST_MACHINE_SIZE);
  result.add(MODE_RESET_MACHINE);

  result.add(MODE_SAVE_PROPERTIES);
  result.add(MODE_SAVE_AS_PROPERTIES);
  result.add(MODE_LOAD_PROPERTIES);
  
  result.add(MODE_INC_SAMPLE_AREA);
  result.add(MODE_DEC_SAMPLE_AREA);
  result.add(MODE_MOVE_IMAGE);
  result.add(MODE_CONVERT_BOX_TO_PICTUREFRAME);
  result.add(MODE_SELECT_PICTUREFRAME);
  result.add(MODE_CLEAR_QUEUE);
  result.add(MODE_EXPORT_QUEUE);
  result.add(MODE_IMPORT_QUEUE);
  result.add(MODE_FIT_IMAGE_TO_BOX);
  result.add(MODE_RESIZE_IMAGE);
  result.add(MODE_RENDER_COMMAND_QUEUE);

  result.add(MODE_CHANGE_GRID_SIZE);
  result.add(MODE_CHANGE_SAMPLE_AREA);

  result.add(MODE_SHOW_IMAGE);
  result.add(MODE_SHOW_DENSITY_PREVIEW);
  result.add(MODE_SHOW_VECTOR);
  result.add(MODE_SHOW_QUEUE_PREVIEW);
  result.add(MODE_SHOW_GUIDES);

  result.add(MODE_CHANGE_MACHINE_WIDTH);
  result.add(MODE_CHANGE_MACHINE_HEIGHT);
  result.add(MODE_CHANGE_MM_PER_REV);
  result.add(MODE_CHANGE_STEPS_PER_REV);
  result.add(MODE_CHANGE_STEP_MULTIPLIER);
  result.add(MODE_CHANGE_PAGE_WIDTH);
  result.add(MODE_CHANGE_PAGE_HEIGHT);
  result.add(MODE_CHANGE_PAGE_OFFSET_X);
  result.add(MODE_CHANGE_PAGE_OFFSET_Y);
  result.add(MODE_CHANGE_PAGE_OFFSET_X_CENTRE);

  result.add(MODE_CHANGE_HOMEPOINT_X);
  result.add(MODE_CHANGE_HOMEPOINT_Y);
  result.add(MODE_CHANGE_HOMEPOINT_X_CENTRE);

  result.add(MODE_CHANGE_PEN_WIDTH);

  result.add(MODE_CHANGE_PEN_TEST_START_WIDTH);
  result.add(MODE_CHANGE_PEN_TEST_END_WIDTH);
  result.add(MODE_CHANGE_PEN_TEST_INCREMENT_SIZE);

  result.add(MODE_SEND_PEN_WIDTH);

  result.add(MODE_CHANGE_MACHINE_MAX_SPEED);
  result.add(MODE_CHANGE_MACHINE_ACCELERATION);
  result.add(MODE_SEND_MACHINE_SPEED);

  result.add(MODE_RENDER_VECTORS);
  result.add(MODE_LOAD_VECTOR_FILE);
  result.add(MODE_IMAGE_PIXEL_BRIGHT_THRESHOLD);
  result.add(MODE_IMAGE_PIXEL_DARK_THRESHOLD);
  result.add(MODE_CHANGE_SERIAL_PORT);
  
  result.add(MODE_SEND_MACHINE_STORE_MODE);
  result.add(MODE_SEND_MACHINE_LIVE_MODE);
  result.add(MODE_SEND_MACHINE_EXEC_MODE);
  
  result.add(MODE_RESIZE_VECTOR);
  result.add(MODE_MOVE_VECTOR);
  result.add(MODE_CHANGE_MIN_VECTOR_LINE_LENGTH);
  
  result.add(MODE_CHOOSE_CHROMA_KEY_COLOUR);
  result.add(MODE_CHANGE_PIXEL_SCALING);
  result.add(MODE_PEN_LIFT_UP);
  result.add(MODE_PEN_LIFT_DOWN);
  result.add(MODE_PEN_LIFT_POS_UP);
  result.add(MODE_PEN_LIFT_POS_DOWN);
  result.add(MODE_SEND_PEN_LIFT_RANGE);
  result.add(MODE_SEND_PEN_LIFT_RANGE_PERSIST);
  
  result.add(MODE_SEND_ROVE_AREA);
  result.add(MODE_SELECT_ROVE_IMAGE_SOURCE);
  result.add(MODE_SEND_START_TEXT);
  result.add(MODE_SHOW_WRITING_DIALOG);
  result.add(MODE_START_SWIRLING);
  result.add(MODE_STOP_SWIRLING);
  result.add(MODE_START_MARKING);
  result.add(MODE_STOP_MARKING);
  result.add(MODE_START_SPRITE);
  result.add(MODE_START_RANDOM_SPRITES);
  result.add(MODE_STOP_RANDOM_SPRITES);
  result.add(MODE_DRAW_NORWEGIAN_DIALOG);
  
  result.add(MODE_LIVE_BLUR_VALUE);
  result.add(MODE_LIVE_SIMPLIFICATION_VALUE);
  result.add(MODE_LIVE_POSTERISE_VALUE);
  result.add(MODE_LIVE_CAPTURE_FROM_LIVE);
  result.add(MODE_LIVE_CONFIRM_DRAW);
  result.add(MODE_LIVE_CANCEL_CAPTURE);
  result.add(MODE_LIVE_ADD_CAPTION);
  result.add(MODE_VECTOR_PATH_LENGTH_HIGHPASS_CUTOFF);
  
  result.add(MODE_SHOW_WEBCAM_RAW_VIDEO);
  result.add(MODE_FLIP_WEBCAM_INPUT);
  result.add(MODE_ROTATE_WEBCAM_INPUT);

  
  return result;
}


/**
 Polargraph controller
 Copyright Sandy Noble 2012.
 
 This file is part of Polargraph Controller.
 
 Polargraph Controller is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Polargraph Controller is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
 
 Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
 Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
 
 This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.
 
 sandy.noble@gmail.com
 http://www.polargraph.co.uk/
 http://code.google.com/p/polargraph/
 */
static final String CMD_CHANGELENGTH = "C01,";
static final String CMD_CHANGEPENWIDTH = "C02,";
static final String CMD_CHANGEMOTORSPEED = "C03,";
static final String CMD_CHANGEMOTORACCEL = "C04,";
static final String CMD_DRAWPIXEL = "C05,";
static final String CMD_DRAWSCRIBBLEPIXEL = "C06,";
static final String CMD_DRAWRECT = "C07,";
static final String CMD_CHANGEDRAWINGDIRECTION = "C08,";
static final String CMD_SETPOSITION = "C09,";
static final String CMD_TESTPATTERN = "C10,";
static final String CMD_TESTPENWIDTHSQUARE = "C11,";
static final String CMD_TESTPENWIDTHSCRIBBLE = "C12,";
static final String CMD_PENDOWN = "C13,";
static final String CMD_PENUP = "C14,";
static final String CMD_DRAWSAWPIXEL = "C15,";
static final String CMD_DRAWROUNDPIXEL = "C16,";
static final String CMD_CHANGELENGTHDIRECT = "C17,";
static final String CMD_TXIMAGEBLOCK = "C18,";
static final String CMD_STARTROVE = "C19,";
static final String CMD_STOPROVE = "C20,";
static final String CMD_SET_ROVE_AREA = "C21,";
static final String CMD_LOADMAGEFILE = "C23,";
static final String CMD_CHANGEMACHINESIZE = "C24,";
static final String CMD_CHANGEMACHINENAME = "C25,";
static final String CMD_REQUESTMACHINESIZE = "C26,";
static final String CMD_RESETMACHINE = "C27,";
static final String CMD_DRAWDIRECTIONTEST = "C28,";
static final String CMD_CHANGEMACHINEMMPERREV = "C29,";
static final String CMD_CHANGEMACHINESTEPSPERREV = "C30,";
static final String CMD_SETMOTORSPEED = "C31,";
static final String CMD_SETMOTORACCEL = "C32,";
static final String CMD_MACHINE_MODE_STORE_COMMANDS = "C33,";
static final String CMD_MACHINE_MODE_EXEC_FROM_STORE = "C34,";
static final String CMD_MACHINE_MODE_LIVE = "C35,";
static final String CMD_RANDOM_DRAW = "C36,";
static final String CMD_SETMACHINESTEPMULTIPLIER = "C37,";
static final String CMD_START_TEXT = "C38,";
static final String CMD_DRAW_SPRITE = "C39,";
static final String CMD_CHANGELENGTH_RELATIVE = "C40,";
static final String CMD_SWIRLING = "C41,";
static final String CMD_DRAW_RANDOM_SPRITE = "C42,";
static final String CMD_DRAW_NORWEGIAN = "C43,";
static final String CMD_DRAW_NORWEGIAN_OUTLINE = "C44,";
static final String CMD_SETPENLIFTRANGE = "C45,";
static final String CMD_SELECT_ROVE_SOURCE_IMAGE = "C46";
static final String CMD_RENDER_ROVE = "C47";

static final int PATH_SORT_NONE = 0;
static final int PATH_SORT_MOST_POINTS_FIRST = 1;
static final int PATH_SORT_GREATEST_AREA_FIRST = 2;
static final int PATH_SORT_CENTRE_FIRST = 3;

private PVector mouseVector = new PVector(0, 0);

Comparator xAscending = new Comparator() 
{
  public int compare(Object p1, Object p2)
  {
    PVector a = (PVector) p1;
    PVector b = (PVector) p2;

    int xValue = new Float(a.x).compareTo(b.x);
    return xValue;
  }
};

Comparator yAscending = new Comparator() 
{
  public int compare(Object p1, Object p2)
  {
    PVector a = (PVector) p1;
    PVector b = (PVector) p2;

    int yValue = new Float(a.y).compareTo(b.y);
    return yValue;
  }
};

public void sendResetMachine()
{
  String command = CMD_RESETMACHINE + "END";
  addToCommandQueue(command);
}
public void sendRequestMachineSize()
{
  String command = CMD_REQUESTMACHINESIZE + "END";
  addToCommandQueue(command);
}
public void sendMachineSpec()
{
  // ask for input to get the new machine size
  String command = CMD_CHANGEMACHINENAME+newMachineName+",END";
  addToCommandQueue(command);
  command = CMD_CHANGEMACHINESIZE+getDisplayMachine().inMM(getDisplayMachine().getWidth())+","+getDisplayMachine().inMM(getDisplayMachine().getHeight())+",END";
  addToCommandQueue(command);
  command = CMD_CHANGEMACHINEMMPERREV+PApplet.parseInt(getDisplayMachine().getMMPerRev())+",END";
  addToCommandQueue(command);
  command = CMD_CHANGEMACHINESTEPSPERREV+PApplet.parseInt(getDisplayMachine().getStepsPerRev())+",END";
  addToCommandQueue(command);
  command = CMD_SETMACHINESTEPMULTIPLIER+machineStepMultiplier+",END";
  addToCommandQueue(command);
  command = CMD_SETPENLIFTRANGE+penLiftDownPosition+","+penLiftUpPosition+",1,END";
  addToCommandQueue(command);

  // speeds
  NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
  DecimalFormat df = (DecimalFormat)nf;  
  df.applyPattern("###.##");
  addToCommandQueue(CMD_SETMOTORSPEED+df.format(currentMachineMaxSpeed)+",1,END");
  addToCommandQueue(CMD_SETMOTORACCEL+df.format(currentMachineAccel)+",1,END");
}

public PVector getMouseVector()
{
  if (mouseVector == null)
  {
    mouseVector = new PVector(0, 0);
  }

  mouseVector.x = mouseX;
  mouseVector.y = mouseY;
  return mouseVector;
}

// Uses the mouse position unless one is specified
public void sendMoveToPosition(boolean direct)
{
  sendMoveToPosition(direct, getMouseVector());
}

public void sendMoveToPosition(boolean direct, PVector position)
{
  String command = null;
  PVector p = getDisplayMachine().scaleToDisplayMachine(position);
  p = getDisplayMachine().inSteps(p);
  p = getDisplayMachine().asNativeCoords(p);
  sendMoveToNativePosition(direct, p);
}

public void sendMoveToNativePosition(boolean direct, PVector p)
{
  String command = null;
  if (direct)
    command = CMD_CHANGELENGTHDIRECT+PApplet.parseInt(p.x+0.5f)+","+PApplet.parseInt(p.y+0.5f)+","+getMaxSegmentLength()+",END";
  else
    command = CMD_CHANGELENGTH+(int)p.x+","+(int)p.y+",END";

  addToCommandQueue(command);
}


public int getMaxSegmentLength()
{
  return this.maxSegmentLength;
}

public void sendTestPattern()
{
  String command = CMD_DRAWDIRECTIONTEST+PApplet.parseInt(gridSize)+",6,END";
  addToCommandQueue(command);
}

public void sendTestPenWidth()
{
  NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
  DecimalFormat df = (DecimalFormat)nf;  
  df.applyPattern("##0.##");
  StringBuilder sb = new StringBuilder();
  sb.append(testPenWidthCommand)
    .append(PApplet.parseInt(gridSize))
      .append(",")
        .append(df.format(testPenWidthStartSize))
          .append(",")
            .append(df.format(testPenWidthEndSize))
              .append(",")
                .append(df.format(testPenWidthIncrementSize))
                  .append(",END");
  addToCommandQueue(sb.toString());
}

public void sendSetPosition()
{
  PVector p = getDisplayMachine().scaleToDisplayMachine(getMouseVector());
  p = getDisplayMachine().convertToNative(p);
  p = getDisplayMachine().inSteps(p);

  String command = CMD_SETPOSITION+PApplet.parseInt(p.x+0.5f)+","+PApplet.parseInt(p.y+0.5f)+",END";
  addToCommandQueue(command);
}

public void sendStartTextAtPoint()
{
  PVector p = getDisplayMachine().scaleToDisplayMachine(getMouseVector());
  p = getDisplayMachine().convertToNative(p);
  p = getDisplayMachine().inSteps(p);

  String command = CMD_START_TEXT+(int)p.x+","+(int)p.y+","+gridSize+",2,END";
  addToCommandQueue(command);
}

public void sendSetHomePosition()
{
  PVector pgCoords = getDisplayMachine().asNativeCoords(getHomePoint());

  String command = CMD_SETPOSITION+PApplet.parseInt(pgCoords.x+0.5f)+","+PApplet.parseInt(pgCoords.y+0.5f)+",END";
  addToCommandQueue(command);
}

public int scaleDensity(int inDens, int inMax, int outMax)
{
  float reducedDens = (PApplet.parseFloat(inDens) / PApplet.parseFloat(inMax)) * PApplet.parseFloat(outMax);
  reducedDens = outMax-reducedDens;
  //  println("inDens:"+inDens+", inMax:"+inMax+", outMax:"+outMax+", reduced:"+reducedDens);

  // round up if bigger than .5
  int result = PApplet.parseInt(reducedDens);
  if (reducedDens - (result) > 0.5f)
    result ++;

  //result = outMax - result;
  return result;
}

public SortedMap<Float, List<PVector>> divideIntoRows(Set<PVector> pixels, int direction)
{
  SortedMap<Float, List<PVector>> inRows = new TreeMap<Float, List<PVector>>();

  for (PVector p : pixels)
  {
    Float row = p.x;
    if (direction == DRAW_DIR_SE || direction == DRAW_DIR_NW)
      row = p.y;

    if (!inRows.containsKey(row))
    {
      inRows.put(row, new ArrayList<PVector>());
    }
    inRows.get(row).add(p);
  }
  return inRows;
}

public PVector sortPixelsInRowsAlternating(SortedMap<Float, List<PVector>> inRows, int initialDirection, float maxPixelSize)
{
  PVector startPoint = null;
  Comparator comp = null;
  boolean rowIsAlongXAxis = true;

  if (initialDirection == DRAW_DIR_SE || initialDirection == DRAW_DIR_NW)
  {
    rowIsAlongXAxis = true;
    comp = xAscending;
  }
  else
  {
    rowIsAlongXAxis = false;
    comp = yAscending;
  }

  // now sort each row, reversing the direction after each row
  boolean reverse = false;
  for (Float rowCoord : inRows.keySet())
  {
    println("row: " + rowCoord);
    List<PVector> row = inRows.get(rowCoord);

    if (reverse)
    {
      // reverse it (descending)
      Collections.sort(row, comp);
      Collections.reverse(row);
      //      if (startPoint == null)
      //      {
      //        if (rowIsAlongXAxis)
      //          startPoint = new PVector(row.get(0).x+(maxPixelSize/2.0), row.get(0).y);
      //        else
      //          startPoint = new PVector(row.get(0).x, row.get(0).y-(maxPixelSize/2.0));
      //      }
      reverse = false;
    }
    else
    {
      // sort row ascending
      Collections.sort(row, comp);
      //      if (startPoint == null)
      //      {
      //        if (rowIsAlongXAxis)
      //          startPoint = new PVector(row.get(0).x-(maxPixelSize/2.0), row.get(0).y);
      //        else
      //          startPoint = new PVector(row.get(0).x, row.get(0).y+(maxPixelSize/2.0));
      //      }
      reverse = true;
    }
  }
  return startPoint;
}

public void sortPixelsInRows(SortedMap<Float, List<PVector>> inRows, int initialDirection)
{
  PVector startPoint = null;
  Comparator comp = null;
  boolean rowIsAlongXAxis = true;

  if (initialDirection == DRAW_DIR_SE || initialDirection == DRAW_DIR_NW)
  {
    rowIsAlongXAxis = true;
    comp = xAscending;
  }
  else
  {
    rowIsAlongXAxis = false;
    comp = yAscending;
  }

  // now sort each row, reversing the direction after each row
  for (Float rowCoord : inRows.keySet())
  {
    println("row: " + rowCoord);
    List<PVector> row = inRows.get(rowCoord);
    // sort row ascending
    Collections.sort(row, comp);

    if (initialDirection == DRAW_DIR_NW || initialDirection == DRAW_DIR_NE)
      Collections.reverse(row);
  }
}



public void sendPixels(Set<PVector> pixels, String pixelCommand, int initialDirection, int startCorner, float maxPixelSize, boolean scaleSizeToDensity)
{

  // sort it into a map of rows, keyed by y coordinate value
  SortedMap<Float, List<PVector>> inRows = divideIntoRows(pixels, initialDirection);

  sortPixelsInRowsAlternating(inRows, initialDirection, maxPixelSize);

  // that was easy.
  // load the queue
  // add a preamble

    // set the first direction
  int drawDirection = initialDirection;
  String changeDir = CMD_CHANGEDRAWINGDIRECTION+getPixelDirectionMode()+"," + drawDirection +",END";
  addToCommandQueue(changeDir);

  // reverse the row sequence if the draw is starting from the bottom
  // and reverse the pixel sequence if it needs to be done (odd number of rows)
  boolean reversePixelSequence = false;
  List<Float> rowKeys = new ArrayList<Float>();
  rowKeys.addAll(inRows.keySet());
  Collections.sort(rowKeys);
  if (startCorner == DRAW_DIR_SE || startCorner == DRAW_DIR_SW)
  {
    Collections.reverse(rowKeys);
    if (rowKeys.size() % 2 == 0)
      reversePixelSequence = true;
  }

  // and move the pen to just next to the first pixel
  List<PVector> firstRow = inRows.get(rowKeys.get(0));

  PVector startPoint = firstRow.get(0);
  int startPointX = PApplet.parseInt(startPoint.x);
  int startPointY = PApplet.parseInt(startPoint.y);
  int halfSize = PApplet.parseInt(maxPixelSize/2.0f);

  print("Dir:");
  if (initialDirection == DRAW_DIR_SE)
  {
    startPointX-=halfSize;
    println("SE");
  }
  else if (initialDirection == DRAW_DIR_SW)
  {
    startPointY-=halfSize;
    println("SW");
  }
  else if (initialDirection == DRAW_DIR_NW)
  {
    startPointX-=halfSize;
    println("NW");
  }
  else if (initialDirection == DRAW_DIR_NE)
  {
    startPointY-=halfSize;
    println("NE");
  }

  if (startPoint != null)
  {
    String touchdown = CMD_CHANGELENGTH+PApplet.parseInt(startPointX)+","+PApplet.parseInt(startPointY)+",END";
    addToCommandQueue(touchdown);
    addToCommandQueue(CMD_PENDOWN+"END");
  }

  boolean penLifted = false;

  // so for each row
  for (Float key : rowKeys)
  {
    List<PVector> row = inRows.get(key);
    if (reversePixelSequence)
      Collections.reverse(row);

    for (PVector v : row)
    {
      if (isHiddenPixel(v)) // check for masked pixels,
      {
        //println("It's outside the bright/dark threshold.");
        if (liftPenOnMaskedPixels)
        {
          if (!penLifted) // if the pen isn't already up
          {
            String raisePen = CMD_PENUP + "END";
            addToCommandQueue(raisePen);
            penLifted = true;
          }
          else
          {
            // println("Pen is already lifted.");
          }
          // now convert to ints 
          int inX = PApplet.parseInt(v.x);
          int inY = PApplet.parseInt(v.y);
          int pixelSize = PApplet.parseInt(maxPixelSize);
          // render a fully bright (255) pixel.
          String command = pixelCommand+inX+","+inY+","+PApplet.parseInt(pixelSize+0.5f)+",255,END";
          addToCommandQueue(command);
        }
        else
        {
          //println("liftPenOnMaskedPixels is not selected.");
        }
        // so this pixel doesn't get added to the queue.
      }
      else // pixel wasn't masked - render it up
      {
        // now convert to ints 
        int inX = PApplet.parseInt(v.x);
        int inY = PApplet.parseInt(v.y);
        Integer density = PApplet.parseInt(v.z);
        int pixelSize = PApplet.parseInt(maxPixelSize);
        if (scaleSizeToDensity)
        {
          pixelSize = scaleDensity(density, 255, PApplet.parseInt(maxPixelSize));
          density = 0;
        }
        int scaledPixelSize = PApplet.parseInt((pixelSize*getPixelScalingOverGridSize())+0.5f);
        String command = pixelCommand+inX+","+inY+","+scaledPixelSize+","+density+",END";

        // put the pen down if lifting over masked pixels is on
        if (liftPenOnMaskedPixels && penLifted)
        {
          //          println("Pen down.");
          String lowerPen = CMD_PENDOWN + "END";
          addToCommandQueue(lowerPen);
          penLifted = false;
        }
        addToCommandQueue(command);
      }
    }

    drawDirection = flipDrawDirection(drawDirection);
    String command = CMD_CHANGEDRAWINGDIRECTION+getPixelDirectionMode()+"," + drawDirection +",END";
    addToCommandQueue(command);
  }

  addToCommandQueue(CMD_PENUP+"END");
  numberOfPixelsTotal = commandQueue.size();
  startPixelTimer();
}


public int flipDrawDirection(int curr)
{
  if (curr == DRAW_DIR_SE)
    return DRAW_DIR_NW;
  else if (curr == DRAW_DIR_NW)
    return DRAW_DIR_SE;
  else if (curr == DRAW_DIR_NE)
    return DRAW_DIR_SW;
  else if (curr == DRAW_DIR_SW)
    return DRAW_DIR_NE;
  else return DRAW_DIR_SE;
}


public int getPixelDirectionMode()
{
  return pixelDirectionMode;
}


public void sendSawtoothPixels(Set<PVector> pixels)
{
  sendPixels(pixels, CMD_DRAWSAWPIXEL, renderStartDirection, renderStartPosition, getGridSize(), false);
}
public void sendCircularPixels(Set<PVector> pixels)
{
  sendPixels(pixels, CMD_DRAWROUNDPIXEL, renderStartDirection, renderStartPosition, getGridSize(), false);
}

public void sendScaledSquarePixels(Set<PVector> pixels)
{
  sendPixels(pixels, CMD_DRAWPIXEL, renderStartDirection, renderStartPosition, getGridSize(), true);
}

public void sendSolidSquarePixels(Set<PVector> pixels)
{
  for (PVector p : pixels)
  {
    if (p.z != MASKED_PIXEL_BRIGHTNESS)
      p.z = 0.0f;
  }
  sendPixels(pixels, CMD_DRAWPIXEL, renderStartDirection, renderStartPosition, getGridSize(), false);
}

public void sendSquarePixels(Set<PVector> pixels)
{
  sendPixels(pixels, CMD_DRAWPIXEL, renderStartDirection, renderStartPosition, getGridSize(), false);
}

public void sendScribblePixels(Set<PVector> pixels)
{
  sendPixels(pixels, CMD_DRAWSCRIBBLEPIXEL, renderStartDirection, renderStartPosition, getGridSize(), false);
}


public void sendOutlineOfPixels(Set<PVector> pixels)
{
  // sort it into a map of rows, keyed by y coordinate value
  SortedMap<Float, List<PVector>> inRows = divideIntoRows(pixels, DRAW_DIR_SE);

  sortPixelsInRowsAlternating(inRows, DRAW_DIR_SE, getGridSize());

  float halfGrid = getGridSize() / 2.0f;
  for (Float key : inRows.keySet())
  {
    for (PVector p : inRows.get(key))
    {
      PVector startPoint = new PVector(p.x-halfGrid, p.y-halfGrid);
      PVector endPoint = new PVector(p.x+halfGrid, p.y+halfGrid);
      String command = CMD_DRAWRECT + PApplet.parseInt(startPoint.x)+","+PApplet.parseInt(startPoint.y)+","+PApplet.parseInt(endPoint.x)+","+PApplet.parseInt(endPoint.y)+",END";
      addToCommandQueue(command);
    }
  }
}

public void sendOutlineOfRows(Set<PVector> pixels, int drawDirection)
{
  // sort it into a map of rows, keyed by y coordinate value
  SortedMap<Float, List<PVector>> inRows = divideIntoRows(pixels, drawDirection);

  sortPixelsInRows(inRows, drawDirection);

  PVector offset = new PVector(getGridSize() / 2.0f, getGridSize() / 2.0f);
  for (Float key : inRows.keySet())
  {
    PVector startPoint = inRows.get(key).get(0);
    PVector endPoint = inRows.get(key).get(inRows.get(key).size()-1);

    if (drawDirection == DRAW_DIR_SE)
    {
      startPoint.sub(offset);
      endPoint.add(offset);
    }
    else if (drawDirection == DRAW_DIR_NW)
    {
      startPoint.add(offset);
      endPoint.sub(offset);
    }
    else if (drawDirection == DRAW_DIR_SW)
    {
      startPoint.add(offset);
      endPoint.sub(offset);
    }
    else if (drawDirection == DRAW_DIR_NW)
    {
      startPoint.add(offset);
      endPoint.sub(offset);
    }

    String command = CMD_DRAWRECT + PApplet.parseInt(startPoint.x)+","+PApplet.parseInt(startPoint.y)+","+PApplet.parseInt(endPoint.x)+","+PApplet.parseInt(endPoint.y)+",END";
    addToCommandQueue(command);
  }
}

public void sendGridOfBox(Set<PVector> pixels)
{
  sendOutlineOfRows(pixels, DRAW_DIR_SE);
  sendOutlineOfRows(pixels, DRAW_DIR_SW);
}


public void sendOutlineOfBox()
{
  // convert cartesian to native format
  PVector tl = getDisplayMachine().inSteps(getBoxVector1());
  PVector br = getDisplayMachine().inSteps(getBoxVector2());

  PVector tr = new PVector(br.x, tl.y);
  PVector bl = new PVector(tl.x, br.y);

  tl = getDisplayMachine().asNativeCoords(tl);
  tr = getDisplayMachine().asNativeCoords(tr);
  bl = getDisplayMachine().asNativeCoords(bl);
  br = getDisplayMachine().asNativeCoords(br);
  
  String cmd = (true) ? CMD_CHANGELENGTHDIRECT : CMD_CHANGELENGTH;

  String command = cmd+(int)tl.x+","+(int)tl.y+","+getMaxSegmentLength()+",END";
  addToCommandQueue(command);

  command = cmd+(int)tr.x+","+(int)tr.y+","+getMaxSegmentLength()+",END";
  addToCommandQueue(command);

  command = cmd+(int)br.x+","+(int)br.y+","+getMaxSegmentLength()+",END";
  addToCommandQueue(command);

  command = cmd+(int)bl.x+","+(int)bl.y+","+getMaxSegmentLength()+",END";
  addToCommandQueue(command);

  command = cmd+(int)tl.x+","+(int)tl.y+","+getMaxSegmentLength()+",END";
  addToCommandQueue(command);
}

public void sendVectorShapes()
{
  sendVectorShapes(getVectorShape(), vectorScaling/100, getVectorPosition(), PATH_SORT_NONE);
}


public void sendVectorShapes(RShape vec, float scaling, PVector position, int pathSortingAlgorithm)
{
  println("Send vector shapes.");
  RPoint[][] pointPaths = vec.getPointsInPaths();      

  // sort the paths to optimise the draw sequence
  switch (pathSortingAlgorithm) {
    case PATH_SORT_MOST_POINTS_FIRST: pointPaths = sortPathsLongestFirst(pointPaths, pathLengthHighPassCutoff); break;
    case PATH_SORT_GREATEST_AREA_FIRST: pointPaths = sortPathsGreatestAreaFirst(vec, pathLengthHighPassCutoff); break;
    case PATH_SORT_CENTRE_FIRST: pointPaths = sortPathsCentreFirst(vec, pathLengthHighPassCutoff); break;
  }

  String command = "";
  PVector lastPoint = new PVector();
  boolean liftToGetToNewPoint = true;

  // go through and get each path
  for (int i = 0; i<pointPaths.length; i++)
  {
    if (pointPaths[i] != null) 
    {
      boolean firstPointFound = false;

      if (pointPaths[i].length > pathLengthHighPassCutoff)
      {
        List<PVector> filteredPoints = filterPoints(pointPaths[i], VECTOR_FILTER_LOW_PASS, minimumVectorLineLength, scaling, position);
        if (!filteredPoints.isEmpty())
        {
          // draw the first one with a pen up and down to get to it
          PVector p = filteredPoints.get(0);
          if ( p.x == lastPoint.x && p.y == lastPoint.y )
            liftToGetToNewPoint = false;
          else
            liftToGetToNewPoint = true;

          // pen UP! (IF THE NEW POINT IS DIFFERENT FROM THE LAST ONE!)
          if (liftToGetToNewPoint)
            addToCommandQueue(CMD_PENUP+"END");
          // move to this point and put the pen down
          command = CMD_CHANGELENGTHDIRECT+(int)p.x+","+(int)p.y+","+getMaxSegmentLength()+",END";
          addToCommandQueue(command);
          if (liftToGetToNewPoint)
            addToCommandQueue(CMD_PENDOWN+"END");



          // then just iterate through the rest
          for (int j=1; j<filteredPoints.size(); j++)
          {
            p = filteredPoints.get(j);
            command = CMD_CHANGELENGTHDIRECT+(int)p.x+","+(int)p.y+","+getMaxSegmentLength()+",END";
            addToCommandQueue(command);
          }
          lastPoint = new PVector(p.x, p.y);
        }
      }
    }
  }
  println("finished.");
}

public RPoint[][] sortPathsLongestFirst(RPoint[][] pointPaths, int highPassCutoff)
{
  // put the paths into a list
  List<RPoint[]> pathsList = new ArrayList<RPoint[]>(pointPaths.length);
  for (int i = 0; i<pointPaths.length; i++)
  {
    if (pointPaths[i] != null) 
    {
      pathsList.add(pointPaths[i]);
    }
  }

  // sort the list
  Collections.sort(pathsList, new Comparator<RPoint[]>() {
    public int compare(RPoint[] o1, RPoint[] o2) {
      if (o1.length > o2.length) {
        return -1;
      } 
      else if (o1.length < o2.length) {
        return 1;
      } 
      else {
        return 0;
      }
    }
  }
  );

  // filter out some short paths
  pathsList = removeShortPaths(pathsList, highPassCutoff);

  // and put them into a new array
  for (int i=0; i<pathsList.size(); i++)
  {
    pointPaths[i] = pathsList.get(i);
  }

  return pointPaths;
}

public RPoint[][] sortPathsGreatestAreaFirst(RShape vec, int highPassCutoff)
{
  // put the paths into a list
  SortedMap<Float, RPoint[]> pathsList = new TreeMap<Float, RPoint[]>();

  int noOfChildren = vec.countChildren();
  for (int i=0; i < noOfChildren; i++)
  {
    float area = vec.children[i].getArea();
    RPoint[] path = vec.children[i].getPointsInPaths()[0];
    pathsList.put(area, path);
  }

  RPoint[][] pointPaths = vec.getPointsInPaths();  
  List<RPoint[]> filtered = new ArrayList<RPoint[]>();
  
  // and put them into a new array
  int i = 0;
  for (Float k : pathsList.keySet())
  {
    if (k >= highPassCutoff)
    {
      filtered.add(pathsList.get(k));
      println("Filtered kept path of area " + k);
    }
    else
      println("Filtered discarded path of area " + k);
  }
  
  pointPaths = new RPoint[filtered.size()][];
  for (i = 0; i < filtered.size(); i++)
  {
    pointPaths[i] = filtered.get(i);
  }

  return pointPaths;
}

public RPoint[][] sortPathsCentreFirst(RShape vec, int highPassCutoff)
{
  // put the paths into a list
  int noOfChildren = vec.countChildren();
  List<RShape> pathsList = new ArrayList<RShape>(noOfChildren);
  for (int i=0; i < noOfChildren; i++)
    pathsList.add(vec.children[i]);
  List<RShape> orderedPathsList = new ArrayList<RShape>(noOfChildren);

  // make a tiny area in the centre of the shape,
  // plan to increment the size of the area until it covers vec entirely
  // (radius of area min = 0, max = distance from shape centre to any corner.)
  
  float aspectRatio = vec.getHeight() / vec.getWidth();
  int n = 0;
  float w = 1.0f;
  float h = w * aspectRatio;
  
  RPoint topLeft = vec.getTopLeft();
  RPoint botRight = vec.getBottomRight();
  
  PVector centre = new PVector(vec.getWidth()/2, vec.getHeight()/2);

  float vecWidth = vec.getWidth();
  
  while (w <= vecWidth)
  {
    w+=6.0f;
    h = w * aspectRatio;
    
    //println(n++ + ". Rect w " + w + ", h " + h);
    RShape field = RShape.createRectangle(centre.x-(w/2.0f), centre.y-(h/2.0f), w, h);
    // add all the shapes that are entirely inside the circle to orderedPathsList
    ListIterator<RShape> it = pathsList.listIterator();
    int shapesAdded = 0;
    while (it.hasNext())
    {
      RShape sh = it.next();
      if (field.contains(sh.getCenter()))
      {
        orderedPathsList.add(sh);
        // remove the shapes from pathsList (so it isn't found again)
        shapesAdded++;
        it.remove();
      }
    }
    // increase the size of the circle and try again
  }

  RPoint[][] pointPaths = new RPoint[orderedPathsList.size()][];// vec.getPointsInPaths();
  for (int i = 0; i < orderedPathsList.size(); i++)
  {
    pointPaths[i] = orderedPathsList.get(i).getPointsInPaths()[0];
  }

  return pointPaths;
}


public List<RPoint[]> removeShortPaths(List<RPoint[]> list, int cutoff)
{
  if (cutoff > 0)
  {
    int numberOfPaths = list.size();
    ListIterator<RPoint[]> it = list.listIterator();
    while (it.hasNext ())
    {
      RPoint[] paths = it.next();
      if (paths == null || cutoff >= paths.length)
      {
        it.remove();
      }
    }
  }
  return list;
}  

public List<PVector> filterPoints(RPoint[] points, int filterToUse, long filterParam, float scaling, PVector position)
{
  return filterPointsLowPass(points, filterParam, scaling, position);
}

public List<PVector> filterPointsLowPass(RPoint[] points, long filterParam, float scaling, PVector position)
{
  List<PVector> result = new ArrayList<PVector>();

  // scale and convert all the points first
  List<PVector> scaled = new ArrayList<PVector>(points.length);
  for (int j = 0; j<points.length; j++)
  {
    RPoint firstPoint = points[j];
    PVector p = new PVector(firstPoint.x, firstPoint.y);
    p = PVector.mult(p, scaling);
    p = PVector.add(p, position);
    p = getDisplayMachine().inSteps(p);
    if (getDisplayMachine().getPage().surrounds(p))
    {
      p = getDisplayMachine().asNativeCoords(p);
      scaled.add(p);
    }
  }

  if (scaled.size() > 1)
  {
    PVector p = scaled.get(0);
    result.add(p);

    for (int j = 1; j<scaled.size(); j++)
    {
      p = scaled.get(j);
      // and even then, only bother drawing if it's a move of over "x" steps
      int diffx = PApplet.parseInt(p.x) - PApplet.parseInt(result.get(result.size()-1).x);
      int diffy = PApplet.parseInt(p.y) - PApplet.parseInt(result.get(result.size()-1).y);

      if (abs(diffx) > filterParam || abs(diffy) > filterParam)
      {
        //println("Adding point " + p + ", last: " + result.get(result.size()-1));
        result.add(p);
      }
    }
  }

  if (result.size() < 2)
    result.clear();

  //println("finished filter.");
  return result;
}




public void sendMachineStoreMode()
{
  String overwrite = ",R";
  if (!getOverwriteExistingStoreFile())
    overwrite = ",A";

  addToRealtimeCommandQueue(CMD_MACHINE_MODE_STORE_COMMANDS + getStoreFilename()+overwrite+",END");
}
public void sendMachineLiveMode()
{
  addToRealtimeCommandQueue(CMD_MACHINE_MODE_LIVE+"END");
}
public void sendMachineExecMode()
{
  sendMachineLiveMode();
  if (storeFilename != null && !"".equals(storeFilename))
    addToCommandQueue(CMD_MACHINE_MODE_EXEC_FROM_STORE + getStoreFilename() + ",END");
}
public void sendRandomDraw()
{
  addToCommandQueue(CMD_RANDOM_DRAW+"END");
}
public void sendStartSwirling()
{
  addToCommandQueue(CMD_SWIRLING+"1,END");
}
public void sendStopSwirling()
{
  addToCommandQueue(CMD_SWIRLING+"0,END");
}
public void sendDrawRandomSprite(String spriteFilename)
{
  addToCommandQueue(CMD_DRAW_RANDOM_SPRITE+","+spriteFilename+",100,500,END");
}


//ControllIO controllIO;
//ControllDevice joypad;
//
//ControllButton buttonA;
//ControllButton buttonB;
//ControllButton buttonX;
//ControllButton buttonY;
//ControllButton buttonL;
//ControllButton buttonR;
//ControllButton buttonStart;
//
//ControllCoolieHat dpad;
//
//List<String> devices = new ArrayList<String>(
//  Arrays.asList("Controller (Xbox 360 Wireless Receiver for Windows)", 
//                "Controller (XBOX 360 For Windows)"));
//
//String signalFromGamepad = null;
//
//static final String BUTTON_A_RELEASED = "ButtonAReleased";
//static final String BUTTON_B_RELEASED = "ButtonBReleased";
//static final String BUTTON_L_RELEASED = "ButtonLReleased";
//static final String BUTTON_R_RELEASED = "ButtonRReleased";
//static final String BUTTON_START_RELEASED = "ButtonStartReleased";
//
//void gamepad_init()
//{
//  controllIO = ControllIO.getInstance(this);
//
//  try
//  {
//    controllIO.printDevices();
//    for (int i = 0; i<devices.size(); i++)
//    {
//      try
//      {
//        println("trying " + i + ": " + devices.get(i));
//        joypad = controllIO.getDevice(devices.get(i));
//        break;
//      }
//      catch (RuntimeException e)
//      {
//        println("Requested device (" + devices.get(i) + ") not found.");
//        joypad = null;
//      }   
//    }
//    
//    if (joypad != null)
//    {
//      joypad.printButtons();
//    
//      buttonA = joypad.getButton("Button 0");
//      buttonB = joypad.getButton("Button 1");
//      buttonX = joypad.getButton("Button 2");
//      buttonY = joypad.getButton("Button 3");
//      
//      buttonL = joypad.getButton("Button 4");
//      buttonR = joypad.getButton("Button 5");
//      
//      buttonStart = joypad.getButton("Button 7");
//      
//      buttonA.plug(this, "buttonARelease", ControllIO.ON_RELEASE);
//      buttonB.plug(this, "buttonBRelease", ControllIO.ON_RELEASE);
//      buttonX.plug(this, "buttonXPress", ControllIO.ON_PRESS);
//      buttonX.plug(this, "buttonXRelease", ControllIO.ON_RELEASE);
//      buttonY.plug(this, "buttonYRelease", ControllIO.ON_RELEASE);
//      
//      buttonL.plug(this, "buttonLRelease", ControllIO.ON_RELEASE);
//      buttonR.plug(this, "buttonRRelease", ControllIO.ON_RELEASE);
//      
//      buttonStart.plug(this, "buttonStartRelease", ControllIO.ON_RELEASE);
//      
//      dpad = joypad.getCoolieHat(10);
//      dpad.setMultiplier(4);
//      dpad.plug(this, "dpadPress", ControllIO.ON_PRESS);
//    }
//  }
//  catch (RuntimeException e)
//  {
//    println("Exception occurred while initialising gamepad: " + e.getMessage());
//  }
//}
//
//public void buttonARelease()
//{
//  signalFromGamepad = BUTTON_A_RELEASED;
//}
//public void buttonBRelease()
//{
//  signalFromGamepad = BUTTON_B_RELEASED;
//}
//public void buttonLRelease()
//{
//  signalFromGamepad = BUTTON_L_RELEASED;
//}
//public void buttonRRelease()
//{
//  signalFromGamepad = BUTTON_R_RELEASED;
//}
//public void buttonStartRelease()
//{
//  signalFromGamepad = BUTTON_START_RELEASED;
//}
//
//void buttonXPress()
//{
//  drawingLiveVideo = true;
//}
//void buttonXRelease()
//{
//  drawingLiveVideo = false;
//}
//void buttonYRelease()
//{
//  flipWebcamImage = !flipWebcamImage;
//}
//
//void dpadPress(float x, float y)
//{
//  float val = dpad.getValue();
//  if (val == 6.0)
//  {
//    liveSimplification--;
//    if (liveSimplification < LIVE_SIMPLIFICATION_MIN)
//      liveSimplification = LIVE_SIMPLIFICATION_MIN;
//  }
//  else if (val == 2.0)
//  {
//    liveSimplification++;
//    if (liveSimplification > LIVE_SIMPLIFICATION_MAX)
//      liveSimplification = LIVE_SIMPLIFICATION_MAX;
//  }
//  if (val == 8.0) // left
//  {
//    pathLengthHighPassCutoff--;
//    if (pathLengthHighPassCutoff < PATH_LENGTH_HIGHPASS_CUTOFF_MIN)
//      pathLengthHighPassCutoff = PATH_LENGTH_HIGHPASS_CUTOFF_MIN;
//  }
//  else if (val == 4.0) // right
//  {
//    pathLengthHighPassCutoff++;
//    if (pathLengthHighPassCutoff > PATH_LENGTH_HIGHPASS_CUTOFF_MAX)
//      pathLengthHighPassCutoff = PATH_LENGTH_HIGHPASS_CUTOFF_MAX;
//  }
//
//  Numberbox n = (Numberbox) getAllControls().get(MODE_LIVE_SIMPLIFICATION_VALUE);
//  n.setValue(liveSimplification);
//  n.update();
//
//  n = (Numberbox) getAllControls().get(MODE_VECTOR_PATH_LENGTH_HIGHPASS_CUTOFF);
//  n.setValue(pathLengthHighPassCutoff);
//  n.update();
//
//}
//
//void processGamepadInput()
//{
//  if (signalFromGamepad != null)
//  {
//    println("Signal from gamepad:  " + signalFromGamepad);
//    if (signalFromGamepad == BUTTON_A_RELEASED)
//    {
//      if (captureShape == null && !confirmedDraw)
//        button_mode_liveCaptureFromLive(); 
//      else if (captureShape != null && !confirmedDraw)
//        button_mode_liveClearCapture();
//      else if (captureShape != null && confirmedDraw)
//      {
//        button_mode_liveClearCapture();
//        button_mode_clearQueue();
//        confirmedDraw = false;
//      }
//    }
//    else if (signalFromGamepad == BUTTON_B_RELEASED)
//    {
//      if (captureShape != null && !confirmedDraw)
//        button_mode_liveConfirmDraw();
//    }
//    else if (signalFromGamepad == BUTTON_L_RELEASED)
//    {
//      commandQueueRunning = !commandQueueRunning;
//    }
//    else if (signalFromGamepad == BUTTON_R_RELEASED)
//    {
//    }
//    else if (signalFromGamepad == BUTTON_START_RELEASED)
//    {
//      preLoadCommandQueue();
//      button_mode_setPositionHome();
//    }
//    
//      
//    // clear the signal  
//    signalFromGamepad = null;
//  }
//  
//}

//void displayGamepadOverlay()
//{
//  textSize(40);
//  fill(255);
//  
//  if (captureShape == null)
//  {
//    image(aButtonImage, width-400, height-180, 128, 128);
//    text("SNAP!", width-400, height-200);
//
//    textSize(30);
//    image(dpadYImage, width-600, height-180, 128, 128);
//    text("Simplify", width-600, height-200);
//
//    image(dpadXImage, width-600, height-400, 128, 128);
//    text("Filter short paths", width-600, height-420);
//  }
//  else
//  {
//    if (confirmedDraw)
//    {
//      image(aButtonImage, width-400, height-180, 128, 128);
//      text("CANCEL!", width-385, height-200);
//    }
//    else
//    {
//      image(aButtonImage, width-400, height-180, 128, 128);
//      text("BACK", width-400, height-200);
//      image(bButtonImage, width-190, height-180, 128, 128);
//      text("DRAW!", width-180, height-200);
//    }
//  }
//  
//  
//  textSize(12);
//}
/**
  Polargraph controller
  Copyright Sandy Noble 2012.

  This file is part of Polargraph Controller.

  Polargraph Controller is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Polargraph Controller is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Polargraph Controller.  If not, see <http://www.gnu.org/licenses/>.
    
  Requires the excellent ControlP5 GUI library available from http://www.sojamo.de/libraries/controlP5/.
  Requires the excellent Geomerative library available from http://www.ricardmarxer.com/geomerative/.
  
  This is an application for controlling a polargraph machine, communicating using ASCII command language over a serial link.

  sandy.noble@gmail.com
  http://www.polargraph.co.uk/
  http://code.google.com/p/polargraph/
*/

public Set<Panel> getPanelsForTab(String tabName)
{
  if (getPanelsForTabs().containsKey(tabName))
  {
    return getPanelsForTabs().get(tabName);
  }
  else
    return new HashSet<Panel>(0);
}

public Map<String, Set<Panel>> buildPanelsForTabs()
{
  Map<String, Set<Panel>> map = new HashMap<String, Set<Panel>>();

  Set<Panel> inputPanels = new HashSet<Panel>(2);
  inputPanels.add(getPanel(PANEL_NAME_INPUT));
  inputPanels.add(getPanel(PANEL_NAME_GENERAL));

  Set<Panel> rovingPanels = new HashSet<Panel>(2);
  rovingPanels.add(getPanel(PANEL_NAME_ROVING));
  rovingPanels.add(getPanel(PANEL_NAME_GENERAL));

  Set<Panel> tracePanels = new HashSet<Panel>(2);
  tracePanels.add(getPanel(PANEL_NAME_TRACE));
  tracePanels.add(getPanel(PANEL_NAME_GENERAL));

  Set<Panel> detailsPanels = new HashSet<Panel>(2);
  detailsPanels.add(getPanel(PANEL_NAME_DETAILS));
  detailsPanels.add(getPanel(PANEL_NAME_GENERAL));

  Set<Panel> queuePanels = new HashSet<Panel>(2);
  queuePanels.add(getPanel(PANEL_NAME_QUEUE));
  queuePanels.add(getPanel(PANEL_NAME_GENERAL));
  
  map.put(TAB_NAME_INPUT, inputPanels);
  map.put(TAB_NAME_ROVING, rovingPanels);
  map.put(TAB_NAME_TRACE, tracePanels);
  map.put(TAB_NAME_DETAILS, detailsPanels);
  map.put(TAB_NAME_QUEUE, queuePanels);
  
  return map;
}

public List<String> buildTabNames()
{
  List<String> list = new ArrayList<String>(5);
  list.add(TAB_NAME_INPUT);
  list.add(TAB_NAME_ROVING);
  list.add(TAB_NAME_TRACE);
  list.add(TAB_NAME_DETAILS);
  list.add(TAB_NAME_QUEUE);
  return list;
}

public void initTabs()
{
  cp5.tab(TAB_NAME_INPUT).setLabel(TAB_LABEL_INPUT);
  cp5.tab(TAB_NAME_INPUT).activateEvent(true);
  cp5.tab(TAB_NAME_INPUT).setId(1);

  cp5.tab(TAB_NAME_DETAILS).setLabel(TAB_LABEL_DETAILS);
  cp5.tab(TAB_NAME_DETAILS).activateEvent(true);
  cp5.tab(TAB_NAME_DETAILS).setId(2);

  cp5.tab(TAB_NAME_ROVING).setLabel(TAB_LABEL_ROVING);
  cp5.tab(TAB_NAME_ROVING).activateEvent(true);
  cp5.tab(TAB_NAME_ROVING).setId(3);

  cp5.tab(TAB_NAME_TRACE).setLabel(TAB_LABEL_TRACE);
  cp5.tab(TAB_NAME_TRACE).activateEvent(true);
  cp5.tab(TAB_NAME_TRACE).setId(4);

  cp5.tab(TAB_NAME_QUEUE).setLabel(TAB_LABEL_QUEUE);
  cp5.tab(TAB_NAME_QUEUE).activateEvent(true);
  cp5.tab(TAB_NAME_QUEUE).setId(5);
}

public Set<String> buildPanelNames()
{
  Set<String> set = new HashSet<String>(6);
  set.add(PANEL_NAME_INPUT);
  set.add(PANEL_NAME_ROVING);
  set.add(PANEL_NAME_TRACE);
  set.add(PANEL_NAME_DETAILS);
  set.add(PANEL_NAME_QUEUE);
  set.add(PANEL_NAME_GENERAL);
  return set;
}

public void trace_initTrace(PImage img)
{
  // dummy initCamera(), does nothing
  //  tracetraceEnabled = true;
  img.loadPixels();
  blob_detector = new BlobDetector(img.width, img.height);
  blob_detector.setResolution(1);
  blob_detector.computeContours(true);
  blob_detector.computeBlobPixels(true);
  blob_detector.setMinMaxPixels(10*10, img.width * img.height);
  
  blob_detector.setBLOBable(new BLOBable_blueBlobs(liveImage));
}

public void trace_initCameraProcCam()
{
//  try
//  {
//    String[] cameras = Capture.list();
//    if (cameras.length > 0) {
//      liveCamera = new Capture(this, 640, 480, cameras[0]);
//      //liveCamera.start();
//      traceEnabled = true;
//    }
//  }
//  catch (Exception e)
//  {
//    println("Exception occurred trying to look for attached webcams.  Webcam will not be used. " + e.getMessage());
//    traceEnabled = false;
//  }

}  
//public PImage trace_buildLiveImage()
//{
//  //liveCamera.start();
//  PImage pimg = createImage(640, 480, RGB);
//  pimg.loadPixels();
//  if (liveCamera.available()) {
//    liveCamera.read();
//  }
//  pimg.pixels = liveCamera.pixels;
//  // flip the image left to right
//  if (flipWebcamImage)
//  {
//
//    List<int[]> list = new ArrayList<int[]>(480);
//
//    for (int r=0; r<pimg.pixels.length; r+=640)
//    {
//      int[] temp = new int[640];
//      for (int c=0; c<640; c++)
//      {
//        temp[c] = pimg.pixels[r+c];
//      }
//      list.add(temp);
//    }
//
//    // reverse the list
//    Collections.reverse(list);
//
//    for (int r=0; r<list.size(); r++)
//    {
//      for (int c=0; c<640; c++)
//      {
//        pimg.pixels[(r*640)+c] = list.get(r)[c];
//      }
//    }
//  }
//  pimg.updatePixels();
//  return pimg;
//}

public PImage trace_processImageForTrace(PImage in)
{
  PImage out = createImage(in.width, in.height, RGB);
  out.loadPixels();
  for (int i = 0; i<in.pixels.length; i++) {
    out.pixels[i] = in.pixels[i];
  }
  out.filter(BLUR, blurValue);
  out.filter(GRAY);
  out.filter(POSTERIZE, posterizeValue);
  out.updatePixels();
  return out;
}

public RShape trace_traceImage(Map<Integer, PImage> seps)
{
  RShape allShapes = null;
  if (seps != null)
  {
    //println("detecting...");
    int i = 0;
    int shapeNo = 1;
    allShapes = new RShape();
    for (Integer key : seps.keySet())
    {
      i++;
      //println("Analysing sep " + i + " of " + seps.size());
      PImage sep = seps.get(key);
      blob_detector.setBLOBable(new BLOBable_blueBlobs(sep));
      blob_detector.update();
      ArrayList<Blob> blob_list = blob_detector.getBlobs();
      for (int blob_idx = 0; blob_idx < blob_list.size(); blob_idx++ ) {
        //println("Getting blob " + blob_idx + " of " + blob_list.size());
        // get the current blob from the blob-list
        Blob blob = blob_list.get(blob_idx);
        // get the list of all the contours from the current blob
        ArrayList<Contour> contour_list = blob.getContours();
        // iterate through the contour_list
        for (int contour_idx = 0; contour_idx < contour_list.size(); contour_idx++ ) {
          // get the current contour from the contour-list
          Contour contour = contour_list.get(contour_idx);

          // example how to simplify a contour
          if (liveSimplification > 0) {
            // can improve speed, if the contour is needed for further work
            ArrayList<Pixel> contour_simple = Polyline.SIMPLIFY(contour, 2, 1);
            // repeat the simplifying process a view more times
            for (int simple_cnt = 0; simple_cnt < liveSimplification; simple_cnt++) {
              contour_simple= Polyline.SIMPLIFY(contour_simple, 2, simple_cnt);
            }
            RShape shp = trace_convertDiewaldToRShape(contour_simple);
            if (shp != null)
            {
              shapeNo++;
              //println("adding shape " + shapeNo + " - blob: " + blob_idx + ", contour: " + contour_idx);
              allShapes.addChild(shp);
            }
          }
          else
          {
            RShape shp = trace_convertDiewaldToRShape(contour.getPixels());
            if (shp != null)
              allShapes.addChild(shp);
          }
        }
      }
    }
  }
  // rotate image
  if (rotateWebcamImage)
  {
    allShapes.rotate(radians(-90));
    // transform it so that top left is at 0,0.
    RPoint topLeft = allShapes.getTopLeft();
    allShapes.translate(-topLeft.x, -topLeft.y);
  }
  return allShapes;
}

public Map<Integer, PImage> trace_buildSeps(PImage img, Integer keyColour)
{
  // create separations
  // pull out number of colours
  Set<Integer> colours = null;
  List<Integer> colourList = null;

  colours = new HashSet<Integer>();
  for (int i=0; i< img.pixels.length; i++) {
    colours.add(img.pixels[i]);
  }
  colourList = new ArrayList(colours);

  Map<Integer, PImage> seps = new HashMap<Integer, PImage>(colours.size());
  for (Integer colour : colours) {
    PImage sep = createImage(img.width, img.height, RGB);
    sep.loadPixels();
    seps.put(colour, sep);
  }

  for (int i = 0; i<img.pixels.length; i++) {
    Integer pixel = img.pixels[i];
    seps.get(pixel).pixels[i] = keyColour;
  }

  return seps;
}

public RShape trace_convertDiewaldToRShape(List<Pixel> points)
{
  RShape shp = null;
  if (points.size() > 2) {
    shp = new RShape();
    Pixel p = points.get(0);
    shp.addMoveTo(PApplet.parseFloat(p.x_), PApplet.parseFloat(p.y_));
    for (int idx = 1; idx < points.size(); idx++) {
      p = points.get(idx);
      shp.addLineTo(PApplet.parseFloat(p.x_), PApplet.parseFloat(p.y_));
    }
    shp.addClose();
  }
  return shp;
}


public void trace_captureCurrentImage(PImage inImage)
{
  captureShape = traceShape;
}

public void trace_captureCurrentImage()
{
//  capturedImage = trace_buildLiveImage();
  trace_captureCurrentImage(getDisplayMachine().getImage());
}

public void trace_processLoadedImage()
{
  trace_captureCurrentImage(getDisplayMachine().getImage());
}

public void trace_saveShape(RShape sh)
{
  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
  String dateCode = sdf.format(new java.util.Date());
  String filename = shapeSavePath + shapeSavePrefix + dateCode + shapeSaveExtension;
  RG.saveShape(filename, sh);
}

//public void stop() {
//  liveCamera.stop();
//  super.stop();
//}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "polargraphcontroller" });
  }
}
