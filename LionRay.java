// LionRay: wav to DFPWM converter
// by Gamax92

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

@SuppressWarnings("serial")
public class LionRay extends JFrame
{
	public static int sampleRate = 32768;
	public static LionRay LionRayJFrame;

	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		LionRayJFrame = new LionRay();
	}

	public static void convert(String inputFilename, String outputFilename) throws UnsupportedAudioFileException, IOException {
		AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 8, 1, 1, sampleRate, false);
		AudioInputStream unconverted = AudioSystem.getAudioInputStream(new File(inputFilename));
		AudioInputStream inFile = AudioSystem.getAudioInputStream(convertFormat, unconverted);
		BufferedOutputStream outFile = new BufferedOutputStream(new FileOutputStream(outputFilename));

		byte[] readBuffer = new byte[1024];
		byte[] outBuffer = new byte[1024/8];
		DFPWM converter = new DFPWM();

		int read;
		while ((read = inFile.read(readBuffer)) > 0) {
			converter.compress(outBuffer, readBuffer, 0, 0, read/8);
		    outFile.write(outBuffer, 0, read/8);
		}
		outFile.close();
	}

	public static JTextField textInputFile, textOutputFile;
	private Container pane;
	private GridBagConstraints c;
	public static JSpinner textRate;

	private void addCtrl(int x, int y, Component something) {
		c.gridx = x;
		c.gridy = y;
		pane.add(something, c);
	}

	private LionRay() {
		JPanel contentPanel = new JPanel();
		Border padding = BorderFactory.createEmptyBorder(2, 4, 2, 4);
		contentPanel.setBorder(padding);
		setContentPane(contentPanel);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/res/icon.png")));

		JLabel labelInputFile = new JLabel("Input File: ");
		JLabel labelOutputFile = new JLabel("Output File: ");

		textInputFile = new JTextField();
		textOutputFile = new JTextField();

		JButton buttonBrowseInput = new JButton("Browse");
		JButton buttonBrowseOutput = new JButton("Browse");
		buttonBrowseInput.addActionListener(new inputBrowseListener());
		buttonBrowseOutput.addActionListener(new outputBrowseListener());

		JLabel labelRate = new JLabel("Samplerate: ");
		textRate = new JSpinner(new SpinnerNumberModel());
		textRate.setEditor(new JSpinner.NumberEditor(textRate, "#"));
		textRate.setValue(sampleRate);

		JButton buttonConvert = new JButton("Convert");
		buttonConvert.addActionListener(new convertListener());

		pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,2,2,2);

		addCtrl(0, 0, labelInputFile);
		c.weightx = 0.5;
		addCtrl(1, 0, textInputFile);
		c.weightx = 0;
		addCtrl(2, 0, buttonBrowseInput);
		addCtrl(0, 1, labelOutputFile);
		addCtrl(1, 1, textOutputFile);
		addCtrl(2, 1, buttonBrowseOutput);
		addCtrl(0, 2, labelRate);
		c.gridwidth = 2;
		addCtrl(1, 2, textRate);
		c.gridwidth = 3;
		addCtrl(0, 3, buttonConvert);

		setTitle("LionRay Wav Converter");
		pack();
		setSize(400,getSize().height);
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}

class inputBrowseListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		FileDialog fileChooser = new FileDialog(LionRay.LionRayJFrame, "Select .wav file to convert", FileDialog.LOAD);
		fileChooser.setFilenameFilter(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) { return name.endsWith(".wav"); }
		});
		fileChooser.setDirectory(".");
		fileChooser.setVisible(true);
		File[] filename = fileChooser.getFiles();
		if(filename.length == 0)
			return;
		LionRay.textInputFile.setText(filename[0].getAbsolutePath());
	}
}

class outputBrowseListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		FileDialog fileChooser = new FileDialog(LionRay.LionRayJFrame, "Select output file", FileDialog.SAVE);
		fileChooser.setFilenameFilter(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) { return name.endsWith(".dfpwm"); }
		});
		fileChooser.setDirectory(".");
		fileChooser.setVisible(true);
		File[] filename = fileChooser.getFiles();
		if(filename.length == 0)
			return;
		LionRay.textOutputFile.setText(filename[0].getAbsolutePath());
	}
}

class convertListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		LionRay.sampleRate = (int) LionRay.textRate.getValue();
		if ((int) LionRay.textRate.getValue() < 0) {
			JOptionPane.showMessageDialog(null, "Sample rate cannot be negative");
			return;
		}
		if ((int) LionRay.textRate.getValue() < 8192)
			JOptionPane.showMessageDialog(null, "Warning, sample rate too low for Computronics");
		if ((int) LionRay.textRate.getValue() > 65536)
			JOptionPane.showMessageDialog(null, "Warning, sample rate too high for Computronics");

		if (LionRay.textInputFile.getText().trim().equals(""))
			JOptionPane.showMessageDialog(null, "No file specified for input");
		else if (!new File(LionRay.textInputFile.getText()).exists())
			JOptionPane.showMessageDialog(null, "Input file does not exists");
		else if (new File(LionRay.textInputFile.getText()).isDirectory())
			JOptionPane.showMessageDialog(null, "Input file is a directory");
		else if (LionRay.textOutputFile.getText().trim().equals(""))
			JOptionPane.showMessageDialog(null, "No file specified for output");
		else if (new File(LionRay.textOutputFile.getText()).isDirectory())
			JOptionPane.showMessageDialog(null, "Output file is a directory");
		else {
			try {
				LionRay.convert(LionRay.textInputFile.getText(), LionRay.textOutputFile.getText());
			} catch (UnsupportedAudioFileException e1) {
				JOptionPane.showMessageDialog(null, "Audio format unsupported");
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "IOException occured, see stdout");
				return;
			}
			JOptionPane.showMessageDialog(null, "Conversion complete");
		}
	}
}

