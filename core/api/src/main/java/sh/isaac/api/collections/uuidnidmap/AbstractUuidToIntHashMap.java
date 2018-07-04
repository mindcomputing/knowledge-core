/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */



package sh.isaac.api.collections.uuidnidmap;

import java.util.OptionalInt;

//~--- JDK imports ------------------------------------------------------------

import java.util.UUID;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.mahout.math.list.IntArrayList;
import org.apache.mahout.math.set.AbstractSet;
import sh.isaac.api.util.UUIDUtil;

//~--- classes ----------------------------------------------------------------

/**
 * The Class AbstractUuidToIntHashMap.
 *
 * @author kec
 */
public abstract class AbstractUuidToIntHashMap
        extends AbstractSet {
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;

   //~--- constructors --------------------------------------------------------

   // public static int hashCollisions = 0; // for debug only

   /**
    * Makes this class non instantiable, but still let's others inherit from it.
    */
   protected AbstractUuidToIntHashMap() {}

   //~--- methods -------------------------------------------------------------

   /**
    * Returns {@code true} if the receiver contains the specified key.
    *
    * @param key the key
    * @return {@code true} if the receiver contains the specified key.
    */
   public boolean containsKey(final long[] key) {
      return !forEachKey(iterKey -> ((key[0] != iterKey[0]) || (key[1] != iterKey[1])));
   }

   /**
    * Returns a deep copy of the receiver; uses
    * {@code clone()} and casts the result.
    *
    * @return a deep copy of the receiver.
    * @throws CloneNotSupportedException the clone not supported exception
    */
   public AbstractUuidToIntHashMap copy()
            throws CloneNotSupportedException {
      return (AbstractUuidToIntHashMap) clone();
   }

   /**
    * Applies a procedure to each key of the receiver, if any. Note: Iterates over the keys in no particular
    * order. Subclasses can define a particular order, for example, "sorted by key". All methods which
    * <i>can</i> be expressed in terms of this method (most methods can) <i>must guarantee</i> to use the
    * <i>same</i> order defined by this method, even if it is no particular order. This is necessary so that,
    * for example, methods {@code keys} and {@code values} will yield association pairs, not two
    * uncorrelated lists.
    *
    * @param procedure the procedure to be applied. Stops iteration if the procedure returns {@code false},
    * otherwise continues.
    * @return {@code false} if the procedure stopped before all keys where iterated over, {@code true}
    * otherwise.
    */
   public abstract boolean forEachKey(UuidProcedure procedure);

   /**
    * Applies a procedure to each (key,value) pair of the receiver, if any. Iteration order is guaranteed to
    * be <i>identical</i> to the order used by method {@link #forEachKey(UuidProcedure)}.
    *
    * @param procedure the procedure to be applied. Stops iteration if the procedure returns {@code false},
    * otherwise continues.
    * @return {@code false} if the procedure stopped before all keys where iterated over, {@code true}
    * otherwise.
    */
   public boolean forEachPair(final UuidIntProcedure procedure) {
      return forEachKey(key -> procedure.apply(key, get(key).getAsInt()));
   }

   /**
    * Returns the first key the given value is associated with. It is often a good idea to first check with
    * {@link #containsValue(int)} whether there exists an association from a key to this value. Search order
    * is guaranteed to be <i>identical</i> to the order used by method {@link #forEachKey(UuidProcedure)}.
    *
    * @param value the value to search for.
    * @return the first key for which holds {@code get(key) == value}; returns {@code Double.NaN} if no
    * such key exists.
    */
   public long[] keyOf(final int value) {
      final long[]  foundKey = new long[2];
      final boolean notFound = forEachPair((long[] iterKey,
                                            int iterValue) -> {
               final boolean found = value == iterValue;

               if (found) {
                  foundKey[0] = iterKey[0];
                  foundKey[1] = iterKey[1];
               }

               return !found;
            });

      if (notFound) {
         return null;
      }

      return foundKey;
   }

   /**
    * Returns a list filled with all keys contained in the receiver. The returned list has a size that equals
    * {@code this.size()}. Note: Keys are filled into the list in no particular order. However, the order is
    * <i>identical</i> to the order used by method {@link #forEachKey(UuidProcedure)}. <p> This method can be
    * used to iterate over the keys of the receiver.
    *
    * @return the keys.
    */
   public UuidArrayList keys() {
      final UuidArrayList list = new UuidArrayList(size());

      keys(list);
      return list;
   }

   /**
    * Fills all keys contained in the receiver into the specified list. Fills the list, starting at index 0.
    * After this call returns the specified list has a new size that equals {@code this.size()}. Iteration
    * order is guaranteed to be <i>identical</i> to the order used by method
    * {@link #forEachKey(UuidProcedure)}. <p> This method can be used to iterate over the keys of the
    * receiver.
    *
    * @param list the list to be filled, can have any size.
    */
   public void keys(final UuidArrayList list) {
      list.clear();
      forEachKey(key -> {
                    list.add(key);
                    return true;
                 });
   }

   /**
    * Fills all pairs satisfying a given condition into the specified lists. Fills into the lists, starting
    * at index 0. After this call returns the specified lists both have a new size, the number of pairs
    * satisfying the condition. Iteration order is guaranteed to be <i>identical</i> to the order used by
    * method {@link #forEachKey(UuidProcedure)}. <p> <b>Example:</b> <br>
    *
    * <pre>
    * UuidIntProcedure condition = new UuidIntProcedure() { // match even values only
    *          public boolean apply(double key, int value) { return value%2==0; }
    *  }
    *  keys = (8,7,6), values = (1,2,2) --> keyList = (6,8), valueList = (2,1)}
    * </pre>
    *
    * @param condition the condition to be matched. Takes the current key as first and the current value as
    * second argument.
    * @param keyList the list to be filled with keys, can have any size.
    * @param valueList the list to be filled with values, can have any size.
    */
   public void pairsMatching(final UuidIntProcedure condition,
                             final UuidArrayList keyList,
                             final IntArrayList valueList) {
      keyList.clear();
      valueList.clear();
      forEachPair((long[] key,
                   int value) -> {
                     if (condition.apply(key, value)) {
                        keyList.add(key);
                        valueList.add(value);
                     }

                     return true;
                  });
   }

   /**
    * Fills all keys and values <i>sorted ascending by key</i> into the specified lists. Fills into the
    * lists, starting at index 0. After this call returns the specified lists both have a new size that
    * equals {@code this.size()}. <p> <b>Example:</b> <br> {@code keys = (8,7,6), values = (1,2,2) --> keyList
    * = (6,7,8), valueList = (2,2,1)}
    *
    * @param keyList the list to be filled with keys, can have any size.
    * @param valueList the list to be filled with values, can have any size.
    */
   public void pairsSortedByKey(final UuidArrayList keyList, final IntArrayList valueList) {
      /*
       * keys(keyList); values(valueList);
       *
       * final double[] k = keyList.elements(); final int[] v =
       * valueList.elements(); org.ihtsdo.Swapper swapper = new
       * org.ihtsdo.Swapper() { public void swap(int a, int b) { int t1; double
       * t2; t1 = v[a]; v[a] = v[b]; v[b] = t1; t2 = k[a]; k[a] = k[b]; k[b] =
       * t2; } };
       *
       * org.ihtsdo.function.IntComparator comp = new
       * org.ihtsdo.function.IntComparator() { public int compare(int a, int b)
       * { return k[a]<k[b] ? -1 : k[a]==k[b] ? 0 : 1; } };
       * org.ihtsdo.MultiSorting.sort(0,keyList.size(),comp,swapper);
       */

      // this variant may be quicker
      // org.ihtsdo.map.OpenDoubleIntHashMap.hashCollisions = 0;
      // System.out.println("collisions="+org.ihtsdo.map.OpenDoubleIntHashMap.hashCollisions);
      keys(keyList);
      keyList.sort();
      valueList.setSize(keyList.size());

      for (int i = keyList.size(); --i >= 0; ) {
         valueList.setQuick(i, get(keyList.getQuick(i)).getAsInt());
      }

      // System.out.println("collisions="+org.ihtsdo.map.OpenDoubleIntHashMap.hashCollisions);
   }

   /**
    * Associates the given key with the given value. Replaces any old {@code (key,someOtherValue)}
    * association, if existing.
    *
    * @param key the key the value shall be associated with.
    * @param value the value to be associated.
    * @return {@code true} if the receiver did not already contain such a key; {@code false} if the
    * receiver did already contain such a key - the new value has now replaced the formerly associated value.
    */
   public abstract boolean put(long[] key, int value);

   /**
    * Put.
    *
    * @param key the key
    * @param value the value
    * @return true, if successful
    */
   public boolean put(UUID key, int value) {
      return put(UUIDUtil.convert(key), value);
   }

   /**
    * Removes the given key with its associated element from the receiver, if present.
    *
    * @param key the key to be removed from the receiver.
    * @return {@code true} if the receiver contained the specified key, {@code false} otherwise.
    */
   public abstract boolean removeKey(long[] key);

   /**
    * Returns a string representation of the receiver, containing the String representation of each key-value
    * pair, sorted ascending by key.
    *
    * @return the string
    */
   @Override
   public String toString() {
      final UuidArrayList theKeys = keys();

      theKeys.sort();

      final StringBuilder buf = new StringBuilder();

      buf.append("[");

      final int maxIndex = theKeys.size() - 1;

      for (int i = 0; i <= maxIndex; i++) {
         final long[] key     = theKeys.get(i);
         final UUID   uuidKey = new UUID(key[0], key[1]);

         buf.append(uuidKey.toString());
         buf.append("->");
         buf.append(String.valueOf(get(key)));

         if (i < maxIndex) {
            buf.append(", ");
         }
      }

      buf.append("]");
      return buf.toString();
   }

   /**
    * Returns a list filled with all values contained in the receiver. The returned list has a size that
    * equals {@code this.size()}. Iteration order is guaranteed to be <i>identical</i> to the order used by
    * method {@link #forEachKey(UuidProcedure)}. <p> This method can be used to iterate over the values of
    * the receiver.
    *
    * @return the values.
    */
   public IntArrayList values() {
      final IntArrayList list = new IntArrayList(size());

      values(list);
      return list;
   }

   /**
    * Fills all values contained in the receiver into the specified list. Fills the list, starting at index
    * 0. After this call returns the specified list has a new size that equals {@code this.size()}.
    * Iteration order is guaranteed to be <i>identical</i> to the order used by method
    * {@link #forEachKey(UuidProcedure)}. <p> This method can be used to iterate over the values of the
    * receiver.
    *
    * @param list the list to be filled, can have any size.
    */
   public void values(final IntArrayList list) {
      list.clear();
      forEachKey(key -> {
                    list.add(get(key).getAsInt());
                    return true;
                 });
   }

   //~--- get methods ---------------------------------------------------------

   /**
    * Returns the value associated with the specified key. It is often a good idea to first check with
    * {@link #containsKey(long[])} whether the given key has a value associated or not, i.e. whether there
    * exists an association for the given key or not.
    *
    * @param key the key to be searched for.
    * @return the value associated with the specified key;
    */
   public abstract OptionalInt get(long[] key);
}

