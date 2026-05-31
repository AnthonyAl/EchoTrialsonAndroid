package com.unipi.alexandris.android.echotrialsonandroid.levelcreator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.unipi.alexandris.android.echotrialsonandroid.R;

/**
 * A custom view that provides a group selection interface.
 * Displays the current group with up/down buttons to navigate through groups.
 * Positioned in the bottom-right corner of the level editor.
 */
public class GroupSelector extends View {

    private static final String ALL_GROUP = "ALL";
    private static final int MAX_GROUP = 1000;
    private final Paint textPaint;
    private final Paint textBackgroundPaint;
    private final Drawable upArrowDrawable;
    private final Drawable downArrowDrawable;
    private int arrowScaleType;
    private float arrowBoxWidth;
    private float arrowBoxHeight;
    private float arrowPadding;
    private final RectF upButtonBounds;
    private final RectF downButtonBounds;
    private final RectF textBounds;
    private String currentGroup = ALL_GROUP;
    private OnGroupSelectedListener listener;
    private boolean upButtonPressed = false;
    private boolean downButtonPressed = false;
    private final ButtonRepeatHelper buttonRepeatHelper;

    public GroupSelector(Context context) {
        this(context, null);
    }

    public GroupSelector(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        
        // Check if we're in design mode (Android Studio designer)
        boolean isInEditMode = isInEditMode();
        
        // Initialize defaults
        int textColor = Color.parseColor("#55302A");
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11, 
            context.getResources().getDisplayMetrics());
        int fontFamily = 0;
        int textStyle = Typeface.BOLD;
        int textAlignment = Paint.Align.CENTER.ordinal();
        int upArrowRes = 0;
        int downArrowRes = 0;
        arrowScaleType = 3; // fitCenter
        arrowBoxWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, 
            context.getResources().getDisplayMetrics());
        arrowBoxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, 
            context.getResources().getDisplayMetrics());
        arrowPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, 
            context.getResources().getDisplayMetrics());
        int buttonBackgroundColor = Color.argb(50, 255, 255, 255);
        int textBackgroundColor = Color.TRANSPARENT;
        
        // Read custom attributes if available
        if (attrs != null) {
            TypedArray a = null;
            try {
                a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.GroupSelector, 0, 0);
                
                // Text styling
                textColor = a.getColor(R.styleable.GroupSelector_groupTextColor, textColor);
                textSize = a.getDimension(R.styleable.GroupSelector_groupTextSize, textSize);
                fontFamily = a.getResourceId(R.styleable.GroupSelector_groupFontFamily, 0);
                textStyle = a.getInt(R.styleable.GroupSelector_groupTextStyle, 1); // bold default
                textAlignment = a.getInt(R.styleable.GroupSelector_groupTextAlignment, 1); // center default
                
                // Arrow drawables
                upArrowRes = a.getResourceId(R.styleable.GroupSelector_upArrowDrawable, 0);
                downArrowRes = a.getResourceId(R.styleable.GroupSelector_downArrowDrawable, 0);
                
                // Arrow scaling and dimensions
                arrowScaleType = a.getInt(R.styleable.GroupSelector_arrowScaleType, arrowScaleType);
                arrowBoxWidth = a.getDimension(R.styleable.GroupSelector_arrowBoxWidth, arrowBoxWidth);
                arrowBoxHeight = a.getDimension(R.styleable.GroupSelector_arrowBoxHeight, arrowBoxHeight);
                arrowPadding = a.getDimension(R.styleable.GroupSelector_arrowPadding, arrowPadding);
                
                // Background colors
                buttonBackgroundColor = a.getColor(R.styleable.GroupSelector_buttonBackgroundColor, buttonBackgroundColor);
                textBackgroundColor = a.getColor(R.styleable.GroupSelector_textBackgroundColor, textBackgroundColor);
                
            } catch (Exception e) {
                // Handle designer preview issues gracefully
            } finally {
                if (a != null) {
                    a.recycle();
                }
            }
        }
        
        // Initialize text paint
        textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        
        // Set text alignment
        Paint.Align[] alignments = {Paint.Align.LEFT, Paint.Align.CENTER, Paint.Align.RIGHT};
        if (textAlignment >= 0 && textAlignment < alignments.length) {
            textPaint.setTextAlign(alignments[textAlignment]);
        } else {
            textPaint.setTextAlign(Paint.Align.CENTER);
        }
        
        // Load font
        Typeface customFont = null;
        if (fontFamily != 0) {
            try {
                customFont = ResourcesCompat.getFont(context, fontFamily);
            } catch (Exception e) {
                // Font loading failed, will use default
            }
        }

        if (customFont == null && fontFamily == 0) {
            try {
                customFont = ResourcesCompat.getFont(context, R.font.aclonica);
            } catch (Exception ignored) {}
        }
        
        // Set typeface with style
        int[] styles = {Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC};
        int finalStyle = (textStyle >= 0 && textStyle < styles.length) ? styles[textStyle] : Typeface.BOLD;
        
        if (customFont != null) {
            textPaint.setTypeface(Typeface.create(customFont, finalStyle));
        } else {
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, finalStyle));
        }
        
        // Initialize background paints
        Paint buttonPaint = new Paint();
        buttonPaint.setColor(buttonBackgroundColor);
        buttonPaint.setStyle(Paint.Style.FILL);
        
        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(textBackgroundColor);
        textBackgroundPaint.setStyle(Paint.Style.FILL);
        
        // Load arrow drawables
        Drawable upArrow = null;
        Drawable downArrow = null;
        
        // Try to load custom drawables first
        if (upArrowRes != 0) {
            try {
                upArrow = ContextCompat.getDrawable(context, upArrowRes);
            } catch (Exception e) {
                // Drawable loading failed
            }
        }
        
        if (downArrowRes != 0) {
            try {
                downArrow = ContextCompat.getDrawable(context, downArrowRes);
            } catch (Exception e) {
                // Drawable loading failed
            }
        }
        
        // Fallback: try to load default arrows if no custom ones specified
        if (upArrow == null && upArrowRes == 0) {
            try {
                upArrow = ContextCompat.getDrawable(context, R.drawable.ui_up);
            } catch (Exception e) {
                // Default drawable not available
            }
        }
        
        if (downArrow == null && downArrowRes == 0) {
            try {
                downArrow = ContextCompat.getDrawable(context, R.drawable.ui_down);
            } catch (Exception e) {
                // Default drawable not available
            }
        }
        
        upArrowDrawable = upArrow;
        downArrowDrawable = downArrow;
        
        // Initialize matrix and bounds
        Matrix arrowMatrix = new Matrix();
        upButtonBounds = new RectF();
        downButtonBounds = new RectF();
        textBounds = new RectF();
        
        // Initialize button repeat helper
        buttonRepeatHelper = new ButtonRepeatHelper();
        
        // Set preview text for designer
        if (isInEditMode) {
            currentGroup = "ALL";
            // Provide better designer preview - ensure font is loaded
            try {
                if (textPaint.getTypeface() == null || textPaint.getTypeface() == Typeface.DEFAULT) {
                    // Try to load the expected font for better preview
                    Typeface previewFont = ResourcesCompat.getFont(context, R.font.aclonica);
                    if (previewFont != null) {
                        textPaint.setTypeface(previewFont);
                    } else {
                        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                }
            } catch (Exception e) {
                textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            }
            // Force a layout pass for the designer
            post(() -> {
                requestLayout();
                invalidate();
            });
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Use custom arrow box dimensions or fallback to equal distribution
        float totalArrowHeight = arrowBoxHeight * 2 + arrowPadding * 3; // padding around and between
        float remainingHeight = h - totalArrowHeight;
        float textHeight = Math.max(remainingHeight, arrowBoxHeight); // ensure text has enough space
        
        // Center everything vertically if there's extra space
        float startY = (h - (arrowBoxHeight + arrowPadding + textHeight + arrowPadding + arrowBoxHeight)) / 2f;
        startY = Math.max(0, startY);
        
        // Set bounds using custom dimensions
        float centerX = w / 2f;
        float arrowLeft = centerX - arrowBoxWidth / 2f;
        float arrowRight = centerX + arrowBoxWidth / 2f;
        
        upButtonBounds.set(arrowLeft, startY, arrowRight, startY + arrowBoxHeight);
        
        float textTop = upButtonBounds.bottom + arrowPadding;
        textBounds.set(0, textTop, w, textTop + textHeight);
        
        float downTop = textBounds.bottom + arrowPadding;
        downButtonBounds.set(arrowLeft, downTop, arrowRight, downTop + arrowBoxHeight);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Provide minimum size for designer preview
        int minWidth = (int) (arrowBoxWidth + arrowPadding * 2);
        int minHeight = (int) (arrowBoxHeight * 2 + arrowBoxHeight + arrowPadding * 4); // two arrows + text + padding
        
        // For edit mode, ensure we have reasonable dimensions
        if (isInEditMode()) {
            minWidth = Math.max(minWidth, 40); // 40dp minimum width
            minHeight = Math.max(minHeight, 90); // 90dp minimum height
        }
        
        int width = resolveSize(minWidth, widthMeasureSpec);
        int height = resolveSize(minHeight, heightMeasureSpec);
        
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Draw text background if visible
        if (textBackgroundPaint.getColor() != Color.TRANSPARENT) {
            canvas.drawRect(textBounds, textBackgroundPaint);
        }
        
        // Draw up arrow drawable with scale type
        if (upArrowDrawable != null) {
            drawScaledDrawable(canvas, upArrowDrawable, upButtonBounds, upButtonPressed);
        } else {
            // Fallback drawing when no drawable is available
            @SuppressLint("DrawAllocation") Paint arrowPaint = new Paint();
            arrowPaint.setColor(textPaint.getColor());
            arrowPaint.setTextAlign(Paint.Align.CENTER);
            float textSize = upButtonBounds.height() * 0.6f;
            if (upButtonPressed) {
                textSize *= 1.1f; // 10% larger when pressed
            }
            arrowPaint.setTextSize(textSize);
            arrowPaint.setAntiAlias(true);
            float arrowY = upButtonBounds.centerY() - ((arrowPaint.descent() + arrowPaint.ascent()) / 2);
            canvas.drawText("▲", upButtonBounds.centerX(), arrowY, arrowPaint);
        }
        
        // Draw text
        if (textBounds.height() > 0) {
            float textY = textBounds.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(currentGroup, textBounds.centerX(), textY, textPaint);
        }
        
        // Draw down arrow drawable with scale type
        if (downArrowDrawable != null) {
            drawScaledDrawable(canvas, downArrowDrawable, downButtonBounds, downButtonPressed);
        } else {
            // Fallback drawing when no drawable is available
            @SuppressLint("DrawAllocation") Paint arrowPaint = new Paint();
            arrowPaint.setColor(textPaint.getColor());
            arrowPaint.setTextAlign(Paint.Align.CENTER);
            float textSize = downButtonBounds.height() * 0.6f;
            if (downButtonPressed) {
                textSize *= 1.1f; // 10% larger when pressed
            }
            arrowPaint.setTextSize(textSize);
            arrowPaint.setAntiAlias(true);
            float arrowY = downButtonBounds.centerY() - ((arrowPaint.descent() + arrowPaint.ascent()) / 2);
            canvas.drawText("▼", downButtonBounds.centerX(), arrowY, arrowPaint);
        }
    }

    private void drawScaledDrawable(Canvas canvas, Drawable drawable, RectF bounds, boolean pressed) {
        // Clear any background color from the drawable
        drawable.setColorFilter(null);
        drawable.clearColorFilter();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            // If no intrinsic size, just fit to bounds
            drawable.setBounds((int) bounds.left, (int) bounds.top, 
                             (int) bounds.right, (int) bounds.bottom);
            drawable.draw(canvas);
            return;
        }
        
        float boundsWidth = bounds.width();
        float boundsHeight = bounds.height();
        
        // Apply press effect by scaling the bounds
        if (pressed) {
            float centerX = bounds.centerX();
            float centerY = bounds.centerY();
            float scale = 1.1f; // 10% larger when pressed
            float scaledWidth = boundsWidth * scale;
            float scaledHeight = boundsHeight * scale;
            bounds = new RectF(
                centerX - scaledWidth / 2f,
                centerY - scaledHeight / 2f,
                centerX + scaledWidth / 2f,
                centerY + scaledHeight / 2f
            );
            boundsWidth = scaledWidth;
            boundsHeight = scaledHeight;
        }
        
        RectF drawBounds = new RectF();
        float scale, scaledWidth, scaledHeight, left, top;
        switch (arrowScaleType) {
            case 0: // matrix - no scaling
                drawBounds.set(bounds.left, bounds.top, 
                             bounds.left + intrinsicWidth, bounds.top + intrinsicHeight);
                break;
                
            case 1: // fitXY - stretch to fill bounds
                drawBounds.set(bounds);
                break;
            case 4: // fitEnd - scale uniformly, align to bottom-right
                scale = Math.min(boundsWidth / intrinsicWidth, boundsHeight / intrinsicHeight);
                scaledWidth = intrinsicWidth * scale;
                scaledHeight = intrinsicHeight * scale;

                if (arrowScaleType == 2) { // fitStart
                    left = bounds.left;
                    top = bounds.top;
                } else if (arrowScaleType == 4) { // fitEnd
                    left = bounds.right - scaledWidth;
                    top = bounds.bottom - scaledHeight;
                } else { // fitCenter
                    left = bounds.left + (boundsWidth - scaledWidth) / 2f;
                    top = bounds.top + (boundsHeight - scaledHeight) / 2f;
                }
                
                drawBounds.set(left, top, left + scaledWidth, top + scaledHeight);
                break;
                
            case 5: // center - no scaling, center
                left = bounds.left + (boundsWidth - intrinsicWidth) / 2f;
                top = bounds.top + (boundsHeight - intrinsicHeight) / 2f;
                drawBounds.set(left, top, left + intrinsicWidth, top + intrinsicHeight);
                break;
                
            case 6: // centerCrop - scale to fill, crop if necessary
                scale = Math.max(boundsWidth / intrinsicWidth, boundsHeight / intrinsicHeight);
                scaledWidth = intrinsicWidth * scale;
                scaledHeight = intrinsicHeight * scale;
                left = bounds.left + (boundsWidth - scaledWidth) / 2f;
                top = bounds.top + (boundsHeight - scaledHeight) / 2f;
                drawBounds.set(left, top, left + scaledWidth, top + scaledHeight);
                break;
                
            case 7: // centerInside - scale down if too big, center
                if (intrinsicWidth <= boundsWidth && intrinsicHeight <= boundsHeight) {
                    // Fits inside, just center
                    left = bounds.left + (boundsWidth - intrinsicWidth) / 2f;
                    top = bounds.top + (boundsHeight - intrinsicHeight) / 2f;
                    drawBounds.set(left, top, left + intrinsicWidth, top + intrinsicHeight);
                } else {
                    // Scale down and center
                    scale = Math.min(boundsWidth / intrinsicWidth, boundsHeight / intrinsicHeight);
                    scaledWidth = intrinsicWidth * scale;
                    scaledHeight = intrinsicHeight * scale;
                    left = bounds.left + (boundsWidth - scaledWidth) / 2f;
                    top = bounds.top + (boundsHeight - scaledHeight) / 2f;
                    drawBounds.set(left, top, left + scaledWidth, top + scaledHeight);
                }
                break;
                
            default:
                drawBounds.set(bounds);
                break;
        }

        canvas.save();
        canvas.clipRect(bounds);
        
        drawable.setBounds((int) drawBounds.left, (int) drawBounds.top, 
                         (int) drawBounds.right, (int) drawBounds.bottom);
        drawable.draw(canvas);
        
        canvas.restore();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (upButtonBounds.contains(x, y)) {
                    upButtonPressed = true;
                    invalidate(); // Redraw with pressed state
                    // Start repeat functionality for up button
                    startButtonRepeat(true);
                    return true;
                } else if (downButtonBounds.contains(x, y)) {
                    downButtonPressed = true;
                    invalidate(); // Redraw with pressed state
                    // Start repeat functionality for down button
                    startButtonRepeat(false);
                    return true;
                }
                break;
                
            case MotionEvent.ACTION_UP:
                if (upButtonPressed && upButtonBounds.contains(x, y)) {
                    incrementGroup();
                }
                if (downButtonPressed && downButtonBounds.contains(x, y)) {
                    decrementGroup();
                }
                // Reset pressed states and stop repeat
                upButtonPressed = false;
                downButtonPressed = false;
                stopButtonRepeat();
                invalidate(); // Redraw without pressed state
                return true;
                
            case MotionEvent.ACTION_CANCEL:
                // Reset pressed states on cancel and stop repeat
                upButtonPressed = false;
                downButtonPressed = false;
                stopButtonRepeat();
                invalidate();
                return true;
                
            case MotionEvent.ACTION_MOVE:
                // Check if we moved outside the button boundaries
                boolean wasUpPressed = upButtonPressed;
                boolean wasDownPressed = downButtonPressed;
                
                if (upButtonPressed && !upButtonBounds.contains(x, y)) {
                    upButtonPressed = false;
                    stopButtonRepeat();
                }
                if (downButtonPressed && !downButtonBounds.contains(x, y)) {
                    downButtonPressed = false;
                    stopButtonRepeat();
                }
                
                // Redraw if state changed
                if (wasUpPressed != upButtonPressed || wasDownPressed != downButtonPressed) {
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void incrementGroup() {
        incrementGroup(1);
    }

    private void incrementGroup(int increment) {
        if (currentGroup.equals(ALL_GROUP)) {
            currentGroup = "0";
        } else {
            int group = Integer.parseInt(currentGroup);
            int newGroup = group + increment;
            if (newGroup <= MAX_GROUP) {
                currentGroup = String.valueOf(newGroup);
            } else {
                currentGroup = String.valueOf(MAX_GROUP);
            }
        }
        notifyGroupSelected();
        invalidate();
    }

    private void decrementGroup() {
        decrementGroup(1);
    }

    private void decrementGroup(int decrement) {
        if (currentGroup.equals("0")) {
            currentGroup = ALL_GROUP;
        } else if (!currentGroup.equals(ALL_GROUP)) {
            int group = Integer.parseInt(currentGroup);
            int newGroup = group - decrement;
            if (newGroup >= 0) {
                currentGroup = String.valueOf(newGroup);
            } else {
                currentGroup = "0";
            }
        }
        notifyGroupSelected();
        invalidate();
    }

    private void startButtonRepeat(boolean isUpButton) {
        // Use the ButtonRepeatHelper to start repeat functionality
        if (buttonRepeatHelper != null) {
            Runnable singleClick = () -> {
                if (isUpButton) {
                    incrementGroup();
                } else {
                    decrementGroup();
                }
            };
            
            Runnable repeatAction = () -> {
                if (isUpButton) {
                    incrementGroup(buttonRepeatHelper.getAcceleratedIncrement(1));
                } else {
                    decrementGroup(buttonRepeatHelper.getAcceleratedIncrement(1));
                }
            };
            
            // We need to manually start the repeat since we're not using ImageButton
            buttonRepeatHelper.buttonPressStartTime = System.currentTimeMillis();
            buttonRepeatHelper.repeatRunnable = new Runnable() {
                @Override
                public void run() {
                    repeatAction.run();
                    buttonRepeatHelper.repeatHandler.postDelayed(this, buttonRepeatHelper.REPEAT_DELAY);
                }
            };
            buttonRepeatHelper.repeatHandler.postDelayed(buttonRepeatHelper.repeatRunnable, 500);
        }
    }

    private void stopButtonRepeat() {
        if (buttonRepeatHelper != null && buttonRepeatHelper.repeatRunnable != null) {
            buttonRepeatHelper.repeatHandler.removeCallbacks(buttonRepeatHelper.repeatRunnable);
            buttonRepeatHelper.repeatRunnable = null;
        }
    }

    public void setOnGroupSelectedListener(OnGroupSelectedListener listener) {
        this.listener = listener;
    }

    private void notifyGroupSelected() {
        if (listener != null) {
            int group = currentGroup.equals(ALL_GROUP) ? -1 : Integer.parseInt(currentGroup);
            listener.onGroupSelected(group);
        }
    }

    public interface OnGroupSelectedListener {
        void onGroupSelected(int group);
    }
} 