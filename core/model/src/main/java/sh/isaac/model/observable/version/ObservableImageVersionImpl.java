package sh.isaac.model.observable.version;

import javafx.beans.property.*;
import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.chronicle.VersionType;
import sh.isaac.api.component.semantic.version.*;
import sh.isaac.api.coordinate.EditCoordinate;
import sh.isaac.api.observable.ObservableVersion;
import sh.isaac.api.observable.semantic.ObservableSemanticChronology;
import sh.isaac.api.observable.semantic.version.ObservableImageVersion;
import sh.isaac.model.observable.CommitAwareObjectProperty;
import sh.isaac.model.observable.ObservableChronologyImpl;
import sh.isaac.model.observable.ObservableFields;
import sh.isaac.model.semantic.SemanticChronologyImpl;
import sh.isaac.model.semantic.version.ImageVersionImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ObservableImageVersionImpl
        extends ObservableAbstractSemanticVersionImpl
        implements ObservableImageVersion {
    /** The long property. */
    SimpleObjectProperty<byte[]> imageDataProperty;

    //~--- constructors --------------------------------------------------------

    /**
     * Instantiates a new observable component nid version impl.
     *
     * @param version the stamped version
     * @param chronology the chronology
     */
    public ObservableImageVersionImpl(ImageVersion version, ObservableSemanticChronology chronology) {
        super(version, chronology);
    }

    public ObservableImageVersionImpl(ObservableImageVersionImpl versionToClone, ObservableSemanticChronology chronology) {
        super(versionToClone, chronology);
        setImageData(versionToClone.getImageData());
    }
    public ObservableImageVersionImpl(UUID primordialUuid, UUID referencedComponentUuid, int assemblageNid) {
        super(VersionType.IMAGE, primordialUuid, referencedComponentUuid, assemblageNid);
    }


    @Override
    public <V extends ObservableVersion> V makeAutonomousAnalog(EditCoordinate ec) {
        ObservableImageVersionImpl analog = new ObservableImageVersionImpl(this, getChronology());
        copyLocalFields(analog);
        analog.setModuleNid(ec.getModuleNid());
        analog.setAuthorNid(ec.getAuthorNid());
        analog.setPathNid(ec.getPathNid());
        return (V) analog;
    }

    //~--- methods -------------------------------------------------------------

    /**
     * Case significance concept nid property.
     *
     * @return the integer property
     */
    @Override
    public ObjectProperty<byte[]> imageDataProperty() {
        if (this.stampedVersionProperty == null && this.imageDataProperty == null) {
            this.imageDataProperty = new CommitAwareObjectProperty(
                    this,
                    ObservableFields.IMAGE_DATA_FOR_SEMANTIC.toExternalString(),
                    0);
        }
        if (this.imageDataProperty == null) {
            this.imageDataProperty = new CommitAwareObjectProperty(
                    this,
                    ObservableFields.IMAGE_DATA_FOR_SEMANTIC.toExternalString(),
                    getImageData());
            this.imageDataProperty.addListener(
                    (observable, oldValue, newValue) -> {
                        ((ImageVersionImpl) this.stampedVersionProperty.get()).setImageData(newValue);
                    });
        }

        return this.imageDataProperty;
    }

    @Override
    public <V extends Version> V makeAnalog(EditCoordinate ec) {
        ImageVersion newVersion = this.stampedVersionProperty.get().makeAnalog(ec);
        ObservableImageVersionImpl newObservableVersion = new ObservableImageVersionImpl(
                newVersion,
                (ObservableSemanticChronology) chronology);

        ((ObservableChronologyImpl) chronology).getVersionList()
                .add(newObservableVersion);
        return (V) newObservableVersion;
    }

    @Override
    public String toString() {
        return "ObservableImageVersionImpl{size:" + getImageData().length + '}';
    }

    @Override
    protected void updateVersion() {
        if (this.imageDataProperty != null && this.imageDataProperty.get() != ((ImageVersionImpl) this.stampedVersionProperty.get()).getImageData()) {
            this.imageDataProperty.set(((ImageVersion) this.stampedVersionProperty.get()).getImageData());
        }
    }

    //~--- get methods ---------------------------------------------------------

    /**
     * Gets the long value.
     *
     * @return the case significance concept nid
     */
    @Override
    public byte[] getImageData() {
        if (this.imageDataProperty != null) {
            return this.imageDataProperty.get();
        }

        return ((ImageVersion) this.stampedVersionProperty.get()).getImageData();
    }

    //~--- set methods ---------------------------------------------------------

    /**
     * Sets the long value.
     *
     * @param imageData the new long value
     */
    @Override
    public final void setImageData(byte[] imageData) {
        if (this.stampedVersionProperty == null) {
            this.imageDataProperty();
        }
        if (this.imageDataProperty != null) {
            this.imageDataProperty.set(imageData);
        }

        if (this.stampedVersionProperty != null) {
            ((MutableImageVersion) this.stampedVersionProperty.get()).setImageData(imageData);
        }
    }

    //~--- get methods ---------------------------------------------------------

    @Override
    public List<ReadOnlyProperty<?>> getProperties() {
        List<ReadOnlyProperty<?>> properties = super.getProperties();

        properties.add(imageDataProperty());
        return properties;
    }

    @Override
    protected List<Property<?>> getEditableProperties3() {
        List<Property<?>> properties = new ArrayList<>();
        properties.add(imageDataProperty());
        return properties;
    }

    @Override
    protected void copyLocalFields(SemanticVersion analog) {
        if (analog instanceof ObservableImageVersionImpl) {
            ObservableImageVersionImpl observableAnalog = (ObservableImageVersionImpl) analog;
            observableAnalog.setImageData(this.getImageData().clone());
        } else if (analog instanceof ImageVersionImpl) {
            ImageVersionImpl simpleAnalog = (ImageVersionImpl) analog;
            simpleAnalog.setImageData(this.getImageData().clone());
        } else {
            throw new IllegalStateException("Can't handle class: " + analog.getClass());
        }
    }

    @Override
    public Chronology createChronologyForCommit(int stampSequence) {
        SemanticChronologyImpl sc = new SemanticChronologyImpl(versionType, getPrimordialUuid(), getAssemblageNid(), this.getReferencedComponentNid());
        ImageVersionImpl newVersion = new ImageVersionImpl(sc, stampSequence);
        copyLocalFields(newVersion);
        sc.addVersion(newVersion);
        return sc;
    }
}