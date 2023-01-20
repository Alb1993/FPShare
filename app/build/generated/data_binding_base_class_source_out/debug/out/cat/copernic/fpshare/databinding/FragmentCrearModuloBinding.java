// Generated by view binder compiler. Do not edit!
package cat.copernic.fpshare.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import cat.copernic.fpshare.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentCrearModuloBinding implements ViewBinding {
  @NonNull
  private final FrameLayout rootView;

  @NonNull
  public final Button btnAddModul;

  @NonNull
  public final ConstraintLayout crearModulo;

  @NonNull
  public final TextInputEditText inputIDModul;

  @NonNull
  public final TextInputLayout inputIDModulMain;

  @NonNull
  public final TextInputEditText inputNombreModul;

  @NonNull
  public final TextInputLayout inputNombreModulMain;

  private FragmentCrearModuloBinding(@NonNull FrameLayout rootView, @NonNull Button btnAddModul,
      @NonNull ConstraintLayout crearModulo, @NonNull TextInputEditText inputIDModul,
      @NonNull TextInputLayout inputIDModulMain, @NonNull TextInputEditText inputNombreModul,
      @NonNull TextInputLayout inputNombreModulMain) {
    this.rootView = rootView;
    this.btnAddModul = btnAddModul;
    this.crearModulo = crearModulo;
    this.inputIDModul = inputIDModul;
    this.inputIDModulMain = inputIDModulMain;
    this.inputNombreModul = inputNombreModul;
    this.inputNombreModulMain = inputNombreModulMain;
  }

  @Override
  @NonNull
  public FrameLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentCrearModuloBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentCrearModuloBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_crear_modulo, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentCrearModuloBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnAddModul;
      Button btnAddModul = ViewBindings.findChildViewById(rootView, id);
      if (btnAddModul == null) {
        break missingId;
      }

      id = R.id.crearModulo;
      ConstraintLayout crearModulo = ViewBindings.findChildViewById(rootView, id);
      if (crearModulo == null) {
        break missingId;
      }

      id = R.id.inputIDModul;
      TextInputEditText inputIDModul = ViewBindings.findChildViewById(rootView, id);
      if (inputIDModul == null) {
        break missingId;
      }

      id = R.id.inputIDModulMain;
      TextInputLayout inputIDModulMain = ViewBindings.findChildViewById(rootView, id);
      if (inputIDModulMain == null) {
        break missingId;
      }

      id = R.id.inputNombreModul;
      TextInputEditText inputNombreModul = ViewBindings.findChildViewById(rootView, id);
      if (inputNombreModul == null) {
        break missingId;
      }

      id = R.id.inputNombreModulMain;
      TextInputLayout inputNombreModulMain = ViewBindings.findChildViewById(rootView, id);
      if (inputNombreModulMain == null) {
        break missingId;
      }

      return new FragmentCrearModuloBinding((FrameLayout) rootView, btnAddModul, crearModulo,
          inputIDModul, inputIDModulMain, inputNombreModul, inputNombreModulMain);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}