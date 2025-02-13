package com.github.bsafwen.sbtcodeartifactcreds.toolWindow

import com.github.bsafwen.sbtcodeartifactcreds.settings.CodeArtifactSettingsComponent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.io.File
import kotlin.concurrent.thread
import com.intellij.icons.AllIcons
import com.intellij.util.ui.UIUtil
import java.awt.*
import javax.swing.*

class CredsGenerationUI : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = ContentFactory.getInstance().createContent(CodeArtifactPanel(project), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

}


class CodeArtifactPanel(private val project: Project) : JPanel() {
    private val domainField = JBTextField()
    private val profileField = JBTextField()
    private val regionField = JBTextField()
    private val generateButton = JButton("Generate Token").apply {
        putClientProperty("JButton.buttonType", "primary")
        icon = AllIcons.Toolwindows.ToolWindowRun
    }
    private val progressBar = JProgressBar().apply {
        isIndeterminate = true
        isVisible = false
        preferredSize = Dimension(preferredSize.width, 4)
    }
    private val statusLabel = createWrappingLabel()

    private fun createWrappingLabel(): JLabel {
        return JLabel("<html><body style='width: 100%'></body></html>").apply {
            foreground = UIUtil.getContextHelpForeground()
            horizontalAlignment = SwingConstants.CENTER
            border = JBUI.Borders.empty(5)
        }
    }

    private fun updateStatusLabel(text: String, icon: Icon?, error: Boolean = false) {
        statusLabel.text = "<html><body style='width: 100%'><div style='text-align: center'>$text</div></body></html>"
        statusLabel.icon = icon
        statusLabel.foreground = if (error) UIUtil.getErrorForeground() else UIUtil.getContextHelpForeground()
    }


    init {
        layout = GridBagLayout()
        border = JBUI.Borders.empty(20)
        setupUI()
        setupActions()
        loadSavedValues()
    }

    private fun setupUI() {
        val gbc = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(5, 5, 5, 5)
            weightx = 1.0
        }

        // Title
        gbc.apply {
            gridy = 0
            gridx = 0
            gridwidth = 2
        }
        add(JBLabel("AWS CodeArtifact Token Generator").apply {
            font = font.deriveFont(Font.BOLD, font.size + 2f)
        }, gbc)

        // Help Panel
        gbc.apply {
            gridy = 1
            insets = Insets(10, 5, 20, 5)
        }
        add(createHelpPanel(), gbc)

        // Input fields panel
        val inputPanel = JPanel(GridBagLayout()).apply {
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.getBoundsColor()),
                JBUI.Borders.empty(15)
            )
        }

        // Domain field
        val domainLabel = JBLabel("Domain:").apply {
            icon = AllIcons.General.Web
        }
        domainField.apply {
            putClientProperty("JTextField.placeholderText", "Enter your CodeArtifact domain")
            document.addDocumentListener(createValidationListener())
        }
        inputPanel.add(domainLabel, createFieldConstraints(0, 0))
        inputPanel.add(domainField, createFieldConstraints(0, 1))

        // Profile field
        val profileLabel = JBLabel("Profile:").apply {
            icon = AllIcons.General.User
        }
        profileField.apply {
            putClientProperty("JTextField.placeholderText", "Enter your AWS profile")
            document.addDocumentListener(createValidationListener())
        }
        inputPanel.add(profileLabel, createFieldConstraints(1, 0))
        inputPanel.add(profileField, createFieldConstraints(1, 1))

        // Region field
        val regionLabel = JBLabel("Region:").apply {
            icon = AllIcons.Nodes.WebFolder
        }
        regionField.apply {
            putClientProperty("JTextField.placeholderText", "e.g., us-east-1")
            document.addDocumentListener(createValidationListener())
        }
        inputPanel.add(regionLabel, createFieldConstraints(2, 0))
        inputPanel.add(regionField, createFieldConstraints(2, 1))

        // Add input panel
        gbc.apply {
            gridy = 2
            insets = Insets(5, 5, 5, 5)
        }
        add(inputPanel, gbc)

        // Progress bar
        gbc.apply {
            gridy = 3
            insets = Insets(10, 5, 5, 5)
        }
        add(progressBar, gbc)

        // Button panel
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
            add(generateButton)
        }
        gbc.apply {
            gridy = 4
            insets = Insets(5, 5, 5, 5)
        }
        add(buttonPanel, gbc)

        // Status label
        gbc.apply {
            gridy = 5
            insets = Insets(5, 5, 5, 5)
        }
        add(statusLabel, gbc)

        // Add filler to push everything up
        gbc.apply {
            gridy = 6
            weighty = 1.0
            fill = GridBagConstraints.BOTH
        }
        add(Box.createVerticalGlue(), gbc)
    }

    private fun createHelpPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIUtil.getBoundsColor()),
            JBUI.Borders.empty(15)
        )
        // Use HTML to format the prerequisites message.
        // The JEditorPane will reflow the content as the panel is resized.
        val html = """
        <html>
          <body>
            <h3>Prerequisites:</h3>
            <ul>
              <li>AWS CLI must be installed</li>
              <li>Valid AWS credentials configured</li>
              <li>Necessary AWS permissions granted</li>
            </ul>
            <p>The token will be saved to ~/.sbt/.credentials</p>
          </body>
        </html>
    """.trimIndent()
        val editorPane = JEditorPane("text/html", html).apply {
            isEditable = false
            // Make the background match so it blends into the panel
            background = panel.background
        }
        panel.add(editorPane, BorderLayout.CENTER)
        return panel
    }


    private fun createFieldConstraints(row: Int, column: Int): GridBagConstraints {
        return GridBagConstraints().apply {
            gridx = column
            gridy = row
            insets = Insets(5, 5, 5, 5)
            fill = GridBagConstraints.HORIZONTAL
            weightx = if (column == 1) 1.0 else 0.0
        }
    }

    private fun createValidationListener() = object : javax.swing.event.DocumentListener {
        override fun insertUpdate(e: javax.swing.event.DocumentEvent) = validateFields()
        override fun removeUpdate(e: javax.swing.event.DocumentEvent) = validateFields()
        override fun changedUpdate(e: javax.swing.event.DocumentEvent) = validateFields()
    }

    private fun validateFields() {
        val isValid = domainField.text.isNotBlank() &&
                profileField.text.isNotBlank() &&
                regionField.text.isNotBlank()
        generateButton.isEnabled = isValid
    }

    private fun setupActions() {
        generateButton.addActionListener {
            thread {
                try {
                    SwingUtilities.invokeLater {
                        generateButton.isEnabled = false
                        progressBar.isVisible = true
                        statusLabel.text = ""  // Clear any previous status
                        updateStatusLabel("", null)
                    }

                    val domain = domainField.text
                    val profile = profileField.text
                    val region = regionField.text

                    saveValues()

                    val command = arrayOf(
                        "aws",
                        "codeartifact",
                        "get-authorization-token",
                        "--domain",
                        domain,
                        "--query",
                        "authorizationToken",
                        "--output",
                        "text",
                        "--profile",
                        profile,
                        "--region",
                        region
                    )

                    val credentialsDir = File(System.getProperty("user.home"), ".sbt")
                    if (!credentialsDir.exists()) {
                        credentialsDir.mkdirs()
                    }

                    val process = ProcessBuilder(*command)
                        .redirectOutput(File(credentialsDir, ".credentials"))
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start()

                    val exitCode = process.waitFor()

                    SwingUtilities.invokeLater {
                        if (exitCode == 0) {
                            updateStatusLabel("Token generated successfully!", AllIcons.General.InspectionsOK)
                        } else {
                            val error = process.errorStream.bufferedReader().readText()
                            updateStatusLabel("Token generation failed!\n" + error, AllIcons.General.InspectionsError)
                            throw Exception("Failed to generate token: $error")
                        }
                    }
                } catch (e: Exception) {
                    SwingUtilities.invokeLater {
                        statusLabel.apply {
                            text = "Error: ${e.message}"
                            icon = AllIcons.General.Error
                            foreground = UIUtil.getErrorForeground()
                        }
                    }
                } finally {
                    SwingUtilities.invokeLater {
                        generateButton.isEnabled = true
                        progressBar.isVisible = false
                    }
                }
            }
        }
    }

    private fun saveValues() {
        val settings = CodeArtifactSettingsComponent.getInstance().state
        settings.domain = domainField.text
        settings.profile = profileField.text
        settings.region = regionField.text
    }

    private fun loadSavedValues() {
        val settings = CodeArtifactSettingsComponent.getInstance().state
        domainField.text = settings.domain
        profileField.text = settings.profile
        regionField.text = settings.region
        validateFields()
    }
}
